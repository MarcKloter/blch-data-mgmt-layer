package bdml.core;

import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import bdml.blockchain.BlockchainFacade;
import bdml.core.cache.CacheImpl;
import bdml.core.domain.exceptions.*;
import bdml.core.helper.*;
import bdml.core.persistence.DeserializationException;
import bdml.core.persistence.Frame;
import bdml.core.persistence.Payload;
import bdml.core.persistence.ProtocolBufferSerializer;
import bdml.core.domain.*;
import bdml.cryptostore.CryptoStoreAdapter;
import bdml.keyserver.KeyServerAdapter;
import bdml.services.Blockchain;
import bdml.core.cache.Cache;
import bdml.services.CryptographicStore;
import bdml.services.KeyServer;
import bdml.core.domain.DataListener;
import bdml.core.domain.Account;
import bdml.services.helper.FrameListener;
import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CoreService implements Core {
    private static final int VERSION = 1;
    private static final int NONCE_BYTES = 4;
    private static final int LISTENER_HANDLE_BYTES = 8;

    private static final String APP_CONFIG = "application.properties";
    private static final String DEFAULT_CONFIG = "default.application.properties";

    private static final Logger LOGGER = LogManager.getLogger(CoreService.class);

    private final Blockchain blockchain;
    private final KeyServer keyServer;
    private final CryptographicStore cryptoStore;
    private final Cache cache;

    private ConcurrentHashMap<AuthenticatedAccount, ConcurrentHashMap<String, DataListener>> handleToListener = new ConcurrentHashMap<>();
    private AtomicBoolean frameListenerRunning = new AtomicBoolean(false);

    private CoreService() {
        Properties configuration = Configuration.load(APP_CONFIG, DEFAULT_CONFIG);
        this.blockchain = new BlockchainFacade(configuration);
        this.keyServer = new KeyServerAdapter(configuration);
        this.cryptoStore = new CryptoStoreAdapter(configuration);
        this.cache = new CacheImpl(configuration);
    }

    public static CoreService getInstance() {
        return Holder.INSTANCE;
    }

    //region Core interface implementation
    //------------------------------------------------------------------------------------------------------------------
    @Override
    public Subject createAccount(String password) {
        Assert.requireNonEmpty(password, "password");

        PublicKey publicKey = cryptoStore.generateKeyPair(password);

        Subject subject = Subject.deriveFrom(publicKey);
        Account account = new Account(subject, password);

        keyServer.registerKey(account.getIdentifier(), publicKey);

        blockchain.createEntity(account);

        // pointer at which the new account will start looking for new frames
        String pointer = blockchain.blockPointer();

        cache.initialize(account, pointer);

        return subject;
    }

    @Override
    public DataIdentifier storeData(Data data, Account account, Set<Subject> subjects) throws AuthenticationException {
        Assert.requireNonNull(data, "data");
        AuthenticatedAccount caller = authenticate(account);

        // resolve all data identifiers to capabilities to attach
        Set<Capability> attachedCapabilities = lookupCapabilities(caller, data.getAttachments());

        // resolve subjects to public keys that will be able to read the data
        Set<PublicKey> recipients = queryPublicKeys(subjects);

        // addCapability owner as recipient
        recipients.add(caller.getPublicKey());

        ParsedFrame frame = assembleFrame(data, recipients, attachedCapabilities);
        byte[] serializedFrame = new ProtocolBufferSerializer().serializeFrame(frame);

        blockchain.storeFrame(caller, frame.getIdentifier().toByteArray(), serializedFrame);
        cache.addCapability(caller, frame.getCapability(), false);
        return frame.getIdentifier();
    }

    @Override
    public DataIdentifier storeData(Data data, Account account) throws AuthenticationException {
        return storeData(data, account, null);
    }

    @Override
    public Set<DataIdentifier> listDirectlyAccessibleData(Account account) throws AuthenticationException {
        AuthenticatedAccount caller = authenticate(account);
        pollNewFrames(caller, false);
        return cache.getAllIdentifiers(caller, false);
    }

    @Override
    public Set<DataIdentifier> listDirectlyAccessibleDataChanges(Account account) throws AuthenticationException {
        AuthenticatedAccount caller = authenticate(account);
        return pollNewFrames(caller, true);
    }

    @Override
    public TreeNode<DataIdentifier> listAttachments(DataIdentifier identifier, Account account) throws AuthenticationException {
        Assert.requireNonNull(identifier, "id");
        AuthenticatedAccount caller = authenticate(account);

        // check whether the entry point exists and the caller has access
        ParsedFrame frame = parseFrame(caller, identifier, getFrame(identifier));

        return getAllAttachments(caller, frame.getCapability()).orElse(null);
    }

    @Override
    public Data getData(DataIdentifier identifier, Account account) throws AuthenticationException {
        Assert.requireNonNull(identifier, "id");
        AuthenticatedAccount caller = authenticate(account);

        ParsedFrame frame = parseFrame(caller, identifier, getFrame(identifier));

        Payload payload = parsePayload(caller, frame);
        if(payload == null) return null;

        Set<DataIdentifier> attachments = payload.getAttachments().stream()
                .map(Capability::getIdentifier)
                .collect(Collectors.toSet());
        return new Data(payload.getData(), attachments);
    }

    @Override
    public String registerDataListener(Account account, DataListener dataListener) throws AuthenticationException {
        Assert.requireNonNull(dataListener, "dataListener");
        AuthenticatedAccount caller = authenticate(account);

        byte[] handleBytes = new byte[LISTENER_HANDLE_BYTES];
        new SecureRandom().nextBytes(handleBytes);
        String handle = Hex.encodeHexString(handleBytes);

        ConcurrentHashMap<String, DataListener> accountListeners = handleToListener.computeIfAbsent(caller, key -> new ConcurrentHashMap<>());

        accountListeners.putIfAbsent(handle, dataListener);

        if (frameListenerRunning.compareAndSet(false, true))
            blockchain.startFrameListener(new CoreFrameListener());

        return handle;
    }

    @Override
    public void unregisterDataListener(Account account, String handle) throws AuthenticationException {
        Assert.requireNonNull(handle, "handle");
        AuthenticatedAccount caller = authenticate(account);

        ConcurrentHashMap<String, DataListener> accountListeners = handleToListener.getOrDefault(caller, null);

        // check whether the handle to remove was registered by the caller
        if (accountListeners != null) {
            accountListeners.remove(handle);
            if (accountListeners.isEmpty())
                handleToListener.remove(caller);

            if (frameListenerRunning.compareAndSet(true, false))
                blockchain.stopFrameListener();
        }
    }

    @Override
    public Map.Entry<DataIdentifier, byte[]> marshalFrame(Data data, Account account, Set<Subject> subjects) throws AuthenticationException {
        Assert.requireNonNull(data, "data");
        AuthenticatedAccount caller = authenticate(account);

        // resolve all data identifiers to capabilities to attach (only checks blockchain, off-chain attachments not allowed)
        Set<Capability> attachedCapabilities = lookupCapabilities(caller, data.getAttachments());

        // resolve subjects to public keys that will be able to read the data
        Set<PublicKey> recipients = queryPublicKeys(subjects);

        // addCapability owner as recipient
        recipients.add(caller.getPublicKey());

        ParsedFrame frame = assembleFrame(data, recipients, attachedCapabilities);
        byte[] serializedFrame = new ProtocolBufferSerializer().serializeFrame(frame);

        return new AbstractMap.SimpleImmutableEntry<>(frame.getIdentifier(), serializedFrame);
    }

    @Override
    public Map.Entry<DataIdentifier, byte[]> marshalFrame(Data data, Account account) throws AuthenticationException {
        return marshalFrame(data, account, null);
    }

    @Override
    public Data unmarshalFrame(DataIdentifier identifier, byte[] frame, Account account) throws AuthenticationException {
        Assert.requireNonNull(identifier, "id");
        Assert.requireNonNull(frame, "frame");
        AuthenticatedAccount caller = authenticate(account);

        Frame deserializedFrame = deserializeFrame(frame);
        if(deserializedFrame == null)
            throw new IllegalArgumentException("The given frame is invalid");

        Optional<Capability> capability = decryptCapability(caller, deserializedFrame, identifier);
        if(capability.isEmpty())
            throw new NotAuthorizedException("The given frame cannot be accessed.");

        ParsedFrame parsedFrame = new ParsedFrame(deserializedFrame, capability.get());

        Payload payload = parsePayload(caller, parsedFrame);
        if(payload == null) return null;

        Set<DataIdentifier> attachments = payload.getAttachments().stream()
                .map(Capability::getIdentifier)
                .collect(Collectors.toSet());

        return new Data(payload.getData(), attachments);
    }
    //------------------------------------------------------------------------------------------------------------------
    //endregion

    /**
     * Authenticates whether the given account exists.
     *
     * @param account identifier and password combination
     * @return {@link AuthenticatedAccount} object containing the account with its public key.
     * @throws AuthenticationException if the given identifier and password combination was wrong or does not exist.
     */
    private AuthenticatedAccount authenticate(Account account) throws AuthenticationException {
        Assert.requireNonNull(account, "account");

        // check whether account exists
        PublicKey publicKey = keyServer.queryKey(account.getIdentifier());

        // check whether the resolved public key and given password correspond to an account
        if (!cryptoStore.checkKeyPair(publicKey, account.getPassword()))
            throw new AuthenticationException();

        return new AuthenticatedAccount(account, publicKey);
    }

    private Set<Capability> lookupCapabilities(AuthenticatedAccount account, Set<DataIdentifier> identifiers) {
        return Optional.ofNullable(identifiers).orElse(Collections.emptySet())
                .stream()
                .filter(Objects::nonNull)
                .map(id -> parseFrame(account, id, getFrame(id)))
                .map(ParsedFrame::getCapability)
                .collect(Collectors.toSet());
    }

    /**
     * Resolves the given addresses to public keys using the keyserver.
     *
     * @param subjects set of {@link Subject}
     * @return Set of {@link PublicKey} corresponding to the given {@code subjects}.
     */
    private Set<PublicKey> queryPublicKeys(Set<Subject> subjects) {
        return Optional.ofNullable(subjects).orElse(Collections.emptySet())
                .stream()
                .map(this::queryPublicKey)
                .collect(Collectors.toSet());
    }

    /**
     * Queries the key server for the public key of the given subject.
     *
     * @param subject {@link Subject}
     * @return {@link PublicKey} corresponding to {@code subject}.
     * @throws IllegalArgumentException if the given {@code subject} is unknown to the key server.
     */
    private PublicKey queryPublicKey(Subject subject) {
        PublicKey pk = keyServer.queryKey(subject.toString());

        return Optional.ofNullable(pk)
                .orElseThrow(() -> new IllegalArgumentException(String.format("There was no subject found for the identifier '%s'.", subject.toString())));
    }

    /**
     * Envelops the given data into a frame for storing.
     *
     * @param data {@link Data} to serialize into the payload
     * @param recipients public keys to encrypt the capability with
     * @param attachedCapabilities capabilities of other frames to add to be payload
     * @return {@link ParsedFrame} containing the capability and {@link Frame}
     */
    private ParsedFrame assembleFrame(Data data, Set<PublicKey> recipients, Set<Capability> attachedCapabilities) {
        // generate nonce of configured length
        byte[] nonce = new byte[NONCE_BYTES];
        new SecureRandom().nextBytes(nonce);

        // build the unencrypted payload: data || attachedCapabilities || nonce
        Payload payload = new Payload(data.getData(), attachedCapabilities, nonce);
        byte[] serializePayload = new ProtocolBufferSerializer().serializePayload(payload);

        // CAP = H(PAYLOAD)
        Capability capability = Capability.of(serializePayload);

        List<byte[]> encryptedCapability = encryptCapability(capability, recipients);

        byte[] encryptedPayload = Crypto.symmetricallyEncrypt(capability, serializePayload);

        // frame that will be persisted
        Frame frame = new Frame(VERSION, encryptedCapability, encryptedPayload);

        return new ParsedFrame(frame, capability);
    }

    /**
     * Encrypts the given capability for every provided recipient.
     *
     * @param capability {@link Capability} to encrypt
     * @param recipients set of public keys to encrypt the capability for
     * @return List of ciphertexts.
     */
    private List<byte[]> encryptCapability(Capability capability, Collection<PublicKey> recipients) {
        List<byte[]> encryptedCapability = new ArrayList<>();

        for (PublicKey recipient : recipients) {
            encryptedCapability.add(cryptoStore.encrypt(recipient, capability.toByteArray()));
        }

        return encryptedCapability;
    }

    private Frame getFrame(DataIdentifier identifier) {
        byte[] serializedFrame = blockchain.getFrame(identifier.toByteArray());
        if(serializedFrame == null)
            throw new IllegalArgumentException(String.format("There was no data found identified by '%s'.", identifier.toString()));

        Frame frame = deserializeFrame(serializedFrame);
        if(frame == null)
            throw new IllegalArgumentException(String.format("There was no data found identified by '%s'.", identifier.toString()));

        return frame;
    }

    private Frame deserializeFrame(byte[] serializedFrame) {
        Frame frame;
        try {
            frame = new ProtocolBufferSerializer().deserializeFrame(serializedFrame);
        } catch (DeserializationException e) {
            LOGGER.error(String.format("Attempted to deserialize a malformed frame: %s", e.getMessage()));
            return null;
        }

        if (frame.getVersion() != VERSION) {
            LOGGER.error(String.format("Attempted to deserialize a frame of version: %s, expected: %s", frame.getVersion(), VERSION));
            return null;
        }

        return frame;
    }

    /**
     * Parses the given {@link Frame} identified through {@code identifier} by looking up the corresponding
     * {@link Capability} in the cache or decrypting it from the {@link Frame#getEncryptedCapability()}.
     *
     * @param account {@link AuthenticatedAccount} to use to retrieve the capability for {@code identifier}
     * @param identifier {@link DataIdentifier} corresponding to the given {@code persistedFrame}
     * @param persistedFrame {@link Frame} retrieved from the blockchain
     * @return {@link ParsedFrame} containing the capability and {@link Frame}
     * @throws NotAuthorizedException if the given {@code account} is not authorized to access the provided {@code persistedFrame}
     */
    private ParsedFrame parseFrame(AuthenticatedAccount account, DataIdentifier identifier, Frame persistedFrame) {
        Capability capability = cache.getCapability(account, identifier).or(() -> {
            Optional<Capability> decryptedCapability = decryptCapability(account, persistedFrame, identifier);
            decryptedCapability.ifPresent(cap -> cache.addCapability(account, cap, false));
            return decryptedCapability;
        }).orElseThrow(() -> new NotAuthorizedException(String.format("The data identified by '%s' could not be accessed.", identifier.toString())));

        return new ParsedFrame(persistedFrame, capability);
    }

    /**
     * Attempts to decrypt a capability matching the provided identifier from a list of ciphertexts using the given account.
     *
     * @param account {@link AuthenticatedAccount} to attempt to decrypt the {@code frame} for
     * @param frame {@link Frame} to retrieve the capability from
     * @param identifier {@link DataIdentifier} to check the H(CAP) = ID property
     * @return {@link Optional} containing the {@link Capability} retrieved from {@code frame} or {@link Optional#empty()}
     */
    private Optional<Capability> decryptCapability(AuthenticatedAccount account, Frame frame, DataIdentifier identifier) {
        List<byte[]> encryptedCapability = frame.getEncryptedCapability();
        Optional<Capability> capability = encryptedCapability.stream()
                // attempt to decrypt the capability (list of ciphertexts)
                .map(ciphertext -> cryptoStore.decrypt(account.getPublicKey(), account.getPassword(), ciphertext))
                .filter(Objects::nonNull)
                // check whether successfully decrypted plaintext are valid capabilities
                .map(plaintext -> {
                    try { return new Capability(plaintext); }
                    catch (CapabilityFormatException e) { return null; }
                })
                .filter(Objects::nonNull)
                .findFirst();

        // check the property H(CAP) = ID
        if (capability.isEmpty() || !capability.get().getIdentifier().equals(identifier))
            return Optional.empty();

        return capability;
    }

    private Payload parsePayload(Account account, ParsedFrame frame) {
        Payload payload;
        try {
            byte[] decPayload = Crypto.symmetricallyDecrypt(frame.getCapability(), frame.getEncryptedPayload());
            payload = new ProtocolBufferSerializer().deserializePayload(decPayload);
        } catch (IllegalArgumentException e) {
            LOGGER.error(String.format("Failed to decrypt the payload of frame '%s': %s", frame.getIdentifier().toString(), e.getMessage()));
            return null;
        } catch (DeserializationException e) {
            LOGGER.error(String.format("Attempted to deserialize the payload of frame '%s': %s", frame.getIdentifier().toString(), e.getMessage()));
            return null;
        }

        // cache all attachments
        Set<Capability> attachments = payload.getAttachments();
        attachments.forEach(cap -> {
            cache.addCapability(account, cap, true);
            cache.addAttachment(account, cap.getIdentifier(), frame.getCapability().getIdentifier());
        });

        return payload;
    }

    private Set<DataIdentifier> pollNewFrames(AuthenticatedAccount account, boolean conductPoll) {
        String currentBlock = blockchain.blockPointer();
        String lastBlock = conductPoll ? cache.getPollPointer(account) : cache.getPointer(account);
        Set<Map.Entry<byte[], byte[]>> newFrames = blockchain.getFrames(lastBlock);

        Set<DataIdentifier> result = new HashSet<>();

        for (Map.Entry<byte[], byte[]> entry : newFrames) {
            byte[] rawIdentifier = entry.getKey();
            byte[] serializedFrame = entry.getValue();

            try {
                DataIdentifier identifier = new DataIdentifier(rawIdentifier);
                Frame frame = deserializeFrame(serializedFrame);
                if(frame != null) {
                    ParsedFrame parsedFrame = parseFrame(account, identifier, frame);
                    Payload payload = parsePayload(account, parsedFrame);

                    if (payload != null)
                        result.add(identifier);
                }
            } catch (DataIdentifierFormatException e) {
                LOGGER.error(String.format("Received an invalid identifier '%s' from blockchain: %s", Hex.encodeHexString(rawIdentifier), e.getMessage()));
            } catch (NotAuthorizedException e) {
                // the frame was not addressed to the given account nor a known attachment
            }
        }

        // move pointer to the current block
        cache.setPointer(account, currentBlock);

        if(conductPoll)
            cache.setPollPointer(account, currentBlock);

        return result;
    }

    private Optional<TreeNode<DataIdentifier>> getAllAttachments(AuthenticatedAccount account, Capability capability) {
        DataIdentifier identifier = capability.getIdentifier();
        if (cache.wasRecursivelyParsed(account, identifier))
            return Optional.ofNullable(cache.getAllAttachments(account, identifier));

        Frame frame;
        try {
            frame = getFrame(identifier);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }

        Payload payload = parsePayload(account, new ParsedFrame(frame, capability));
        if(payload == null) return Optional.empty();

        Set<TreeNode<DataIdentifier>> attachments = new HashSet<>();
        for (Capability attachment : payload.getAttachments()) {
            // recursively loop through all attachments
            getAllAttachments(account, attachment).ifPresent(attachments::add);

            cache.addAttachment(account, attachment.getIdentifier(), identifier);
            cache.setRecursivelyParsed(account, attachment.getIdentifier());
        }

        return Optional.of(new TreeNode<>(identifier, attachments));
    }

    // initialize-on-demand holder
    private static class Holder {
        private static final CoreService INSTANCE = new CoreService();
    }

    public class CoreFrameListener implements FrameListener {
        @Override
        public void update(byte[] rawIdentifier, byte[] serializedFrame) {
            DataIdentifier identifier;
            try {
                identifier = new DataIdentifier(rawIdentifier);
            } catch (DataIdentifierFormatException e) {
                LOGGER.error(String.format("Received an invalid identifier '%s' from blockchain: %s", Hex.encodeHexString(rawIdentifier), e.getMessage()));
                return;
            }

            Frame persistedFrame = deserializeFrame(serializedFrame);
            if(persistedFrame == null) return;

            for (AuthenticatedAccount account : handleToListener.keySet()) {
                ParsedFrame parsedFrame;
                try {
                    parsedFrame = parseFrame(account, identifier, persistedFrame);
                } catch (NotAuthorizedException e) {
                    continue;
                }

                Payload payload = parsePayload(account, parsedFrame);
                if(payload == null) continue;

                ConcurrentHashMap<String, DataListener> accountListeners = handleToListener.getOrDefault(account, null);
                if (accountListeners != null && !accountListeners.isEmpty()) {
                    for (DataListener dataListener : accountListeners.values()) {
                        dataListener.update(identifier.toString());
                    }
                }
            }
        }
    }
}
