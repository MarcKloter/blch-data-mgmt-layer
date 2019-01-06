package bdml.core;

import java.security.PublicKey;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import bdml.blockchain.BlockchainFacade;
import bdml.cache.CacheImpl;
import bdml.core.helper.*;
import bdml.core.proto.FrameOuterClass;
import bdml.cryptostore.CryptoStoreAdapter;
import bdml.keyserver.KeyServerAdapter;
import bdml.services.Blockchain;
import bdml.services.Cache;
import bdml.services.CryptographicStore;
import bdml.services.KeyServer;
import bdml.services.api.exceptions.AuthenticationException;
import bdml.services.api.exceptions.NotAuthorizedException;
import bdml.services.api.types.Account;
import bdml.services.api.types.Data;
import bdml.services.api.types.Filter;
import bdml.services.api.Core;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

public class CoreService implements Core {
    // TODO: load constants from configuration file
    private final int VERSION = 1;

    private Blockchain blockchain = new BlockchainFacade();
    private KeyServer keyServer = new KeyServerAdapter();
    private CryptographicStore cryptoStore = new CryptoStoreAdapter();
    private Cache cache = new CacheImpl();

    //region Core interface implementation
    //------------------------------------------------------------------------------------------------------------------

    @Override
    public String storeData(String data, List<String> attachments, Account account, List<String> subjects) throws AuthenticationException {
        Assert.requireNonEmpty(data, "data");

        // check provided account
        KnownAccount caller = checkAccount(account);

        // resolve the attachments (list of data identifiers) to capabilities using the given account (requires access to the data)
        Set<byte[]> attachedCapabilities = resolveAllIdentifiers(caller, attachments);

        // resolve subjects to public keys that will be able to read the data
        Set<PublicKey> recipients = resolveAllSubjects(subjects);

        // add owner as recipient
        recipients.add(caller.getPublicKey());

        // TODO: save data to IPFS

        FrameWrapper frame = assembleFrame(data, recipients, attachedCapabilities);

        // persist frame in blockchain
        blockchain.storeFrame(account, frame.getIdentifier(), frame.getBytes());

        // cache capability
        cache.add(account, frame.getIdentifier(), frame.getCapability());

        return Hex.encodeHexString(frame.getIdentifier());
    }

    @Override
    public String storeData(String data, Account account, List<String> subjects) throws AuthenticationException {
        return storeData(data, null, account, subjects);
    }

    @Override
    public String storeData(String data, Account account) throws AuthenticationException {
        return storeData(data, null, account, null);
    }

    @Override
    public String storeData(Data data, Account account, List<String> subjects) throws AuthenticationException {
        return storeData(data.getData(), data.getAttachments(), account, subjects);
    }

    @Override
    public String storeData(Data data, Account account) throws AuthenticationException {
        return storeData(data.getData(), data.getAttachments(), account, null);
    }

    @Override
    public List<String> listData(Account account, Filter filter) throws AuthenticationException {
        // check provided account
        KnownAccount caller = checkAccount(account);

        return null;
    }

    @Override
    public Data getData(String identifier, Account account) throws AuthenticationException {
        Assert.requireNonEmpty(identifier, "id");

        // validate identifier format
        byte[] idBytes = decodeIdentifier(identifier);

        // check provided account
        KnownAccount caller = checkAccount(account);

        // retrieve data frame from blockchain
        Collection<byte[]> frames = blockchain.getFrames(idBytes);

        // check cache for capability
        byte[] capability = cache.get(account, idBytes);

        Optional<FrameWrapper> frame = parseFrames(caller, idBytes, capability, frames);

        return frame.map(f -> decryptPayload(f, account)).orElse(null);
    }

    @Override
    public List<String> listSubjects() {
        // TODO: implement
        List<String> result = new ArrayList<>();
        // result.add(new Subject());
        return result;
    }

    @Override
    public String createAccount(String password) {
        Assert.requireNonEmpty(password, "password");

        // create key pair for the account
        PublicKey publicKey = cryptoStore.generateKeyPair(password);

        // take 160 LSB in hex representation as account identifier
        byte[] pkBytes = publicKey.getEncoded();
        byte[] idBytes = Arrays.copyOfRange(pkBytes, pkBytes.length - 20, pkBytes.length);
        String identifier = Hex.encodeHexString(idBytes);

        // pass created public key to the key server for distribution
        keyServer.registerKey(identifier, publicKey);

        // create an account on the blockchain tied to id and pwd
        blockchain.createEntity(new Account(identifier, password));
        // TODO: createEntity return the address to transfer coins (not dev chain) - what to do?

        return identifier;
    }

    //------------------------------------------------------------------------------------------------------------------
    //endregion

    /**
     * Checks whether the given account exists.
     *
     * @param account identifier and password combination
     * @return Object containing the account with its public key.
     * @throws AuthenticationException if the given identifier and password combination was wrong or does not exist.
     */
    private KnownAccount checkAccount(Account account) throws AuthenticationException {
        Assert.requireNonNull(account, "account");

        // check whether account exists
        PublicKey publicKey = keyServer.queryKey(account.getIdentifier());

        // check whether the resolved public key and given password correspond to an account
        if (!cryptoStore.checkKeyPair(publicKey, account.getPassword()))
            throw new AuthenticationException();

        return new KnownAccount(account, publicKey);
    }

    /**
     * Decodes the given 32 bytes identifier from hex representation to a byte array.
     *
     * @param identifier hex string representation of an identifier
     * @return The byte array of the given identifier.
     * @throws IllegalArgumentException if {@code identifier} has an invalid format.
     */
    private byte[] decodeIdentifier(String identifier) {
        try {
            byte[] idBytes = Hex.decodeHex(identifier);

            // check whether the decoded identifier is valid
            Assert.require32Bytes(idBytes);

            return idBytes;
        } catch (DecoderException e) {
            throw new IllegalArgumentException(String.format("The identifier '%s' is invalid: %s.", identifier, e.getMessage()));
        }
    }

    private Set<byte[]> resolveAllIdentifiers(KnownAccount account, Collection<String> identifiers) {
        // create empty list if no identifiers are present
        identifiers = Optional.ofNullable(identifiers).orElse(new ArrayList<>());

        Set<byte[]> capabilities = new HashSet<>();
        identifiers.stream()
                .distinct() // distinct to avoid unnecessary operations
                .map(this::decodeIdentifier) // decode identifier to valid byte array
                .map(id -> resolveIdentifier(account, id))
                .collect(Collectors.toCollection(() -> capabilities));
        return capabilities;
    }

    private byte[] resolveIdentifier(KnownAccount account, byte[] identifier) {
        // check cache for capability
        byte[] capability = cache.get(account, identifier);

        // if the capability was not found in the cache, retrieve the corresponding data from the blockchain
        if (capability == null) {
            Collection<byte[]> frames = blockchain.getFrames(identifier);

            if (frames.isEmpty())
                throw new IllegalArgumentException(String.format("There was no data found identified by '%s'.", Hex.encodeHexString(identifier)));

            Optional<FrameWrapper> frame = parseFrames(account, identifier, capability, frames);
            capability = frame.map(FrameWrapper::getCapability)
                    .orElseThrow(() -> new NotAuthorizedException("The data to be attached could not be accessed."));
        }

        return capability;
    }

    /**
     * Resolves the given addresses to public keys using the keyserver.
     *
     * @param subjects list of addresses
     * @return Set of distinct public keys corresponding to the given subjects.
     */
    private Set<PublicKey> resolveAllSubjects(List<String> subjects) {
        // create empty list if no subjects are present
        subjects = Optional.ofNullable(subjects).orElse(new ArrayList<>());

        Set<PublicKey> resolvedSubjects = new HashSet<>();
        subjects.stream()
                .distinct() // distinct to avoid unnecessary operations
                .map(this::resolveSubject)
                .collect(Collectors.toCollection(() -> resolvedSubjects));
        return resolvedSubjects;
    }

    /**
     * Resolves identifiers known to the key server to public keys.
     *
     * @param identifier string representation of an identifier for a subject
     * @return Public key corresponding to the provided identifier.
     * @throws IllegalArgumentException if the given identifier is unknown to the key server.
     */
    private PublicKey resolveSubject(String identifier) {
        PublicKey pk = keyServer.queryKey(identifier);

        return Optional.ofNullable(pk)
                .orElseThrow(() -> new IllegalArgumentException(String.format("There was no subject found for the identifier '%s'.", identifier)));
    }

    private Optional<FrameWrapper> parseFrames(KnownAccount account, byte[] identifier, byte[] capability, Collection<byte[]> frames) {
        for(byte[] frame : frames) {
            Optional<FrameWrapper> frameWrapper = Optional.ofNullable(disassembleFrame(account, identifier, capability, frame));
            if(frameWrapper.isPresent())
                return frameWrapper;
        }
        return Optional.empty();
    }

    /**
     * Envelops the given data into a frame for storing.
     *
     * @param data
     * @param recipients
     * @param attachedCapabilities
     * @return
     */
    private FrameWrapper assembleFrame(String data, Set<PublicKey> recipients, Set<byte[]> attachedCapabilities) {
        // build the unencrypted payload: data || attachedCapabilities
        byte[] payload = Protobuf.buildPayload(data, attachedCapabilities);

        // CAP = H(PAYLOAD)
        byte[] capability = Crypto.hashValue(payload);

        // ID = H(CAP)
        byte[] identifier = Crypto.hashValue(capability);

        // encrypt CAP for each recipient PK
        List<byte[]> encryptedCapability = encryptCapability(capability, recipients);

        // symmetrically encrypt payload using the capability
        byte[] encryptedPayload = Crypto.symmetricallyEncrypt(capability, payload);

        FrameOuterClass.Frame frame = Protobuf.buildFrame(VERSION, encryptedCapability, encryptedPayload);

        return new FrameWrapper(identifier, capability, frame);
    }

    /**
     * Takes apart the given protocol buffer frame message.
     *
     * @param account account containing public key and password for the crypto store to decrypt the capability if needed
     * @param identifier 32 bytes data identifier
     * @param capability cached capability or null
     * @param frame
     * @return
     */
    private FrameWrapper disassembleFrame(KnownAccount account, byte[] identifier, byte[] capability, byte[] frame) {
        // get protocol buffer frame message object for the configured version
        FrameOuterClass.Frame protoFrame = Protobuf.parseFrame(frame, VERSION);

        // check whether a capability was provided (from cache) otherwise decrypt it from the frame
        capability = Optional.ofNullable(capability).orElse(decryptCapability(account, protoFrame, identifier));

        // wrap the frame with meta information if the given account is able to decrypt its content
        return capability != null ? new FrameWrapper(identifier, capability, protoFrame) : null;
    }

    /**
     * Encrypts the given capability for every provided recipient.
     *
     * @param capability capability to encrypt
     * @param recipients set of public keys to encrypt the capability for
     * @return List of ciphertexts.
     */
    private List<byte[]> encryptCapability(byte[] capability, Collection<PublicKey> recipients) {
        List<byte[]> encryptedCapability = new ArrayList<>();

        for (PublicKey recipient : recipients) {
            encryptedCapability.add(cryptoStore.encrypt(recipient, capability));
        }

        return encryptedCapability;
    }

    /**
     * Attempts to decrypt a capability matching the provided identifier from a list of ciphertexts using the given account.
     *
     * @param account
     * @param frame
     * @param identifier
     * @return
     */
    private byte[] decryptCapability(KnownAccount account, FrameOuterClass.Frame frame, byte[] identifier) {
        List<byte[]> encCapList = Protobuf.decode(frame.getEncryptedCapabilityList());
        byte[] capability = encCapList.stream()
                .map(ciphertext -> cryptoStore.decrypt(account.getPublicKey(), account.getPassword(), ciphertext))
                .filter(Objects::nonNull)
                .findFirst().orElse(null);

        // check the property H(CAP) = ID
        if(capability == null || Arrays.equals(Crypto.hashValue(capability), identifier))
            return null;

        // add capability to cache
        cache.add(account, identifier, capability);

        return capability;
    }

    private Data decryptPayload(FrameWrapper frame, Account account) {
        // retrieve encrypted payload
        byte[] encPayload = Protobuf.decode(frame.unwrap().getEncryptedPayload());
        // decrypt the payload using the capability
        byte[] decPayload = Crypto.symmetricallyDecrypt(frame.getCapability(), encPayload);
        // get protocol buffer payload message object
        FrameOuterClass.Payload payload = Protobuf.parsePayload(decPayload);

        List<byte[]> capabilities = Protobuf.decode(payload.getAttachedCapabilityList());
        List<String> identifiers = capabilities.stream().map(Crypto::hashValue).map(Hex::encodeHexString).collect(Collectors.toList());

        // cache attached capabilities
        capabilities.forEach(cap -> cache.add(account, Crypto.hashValue(cap), cap));

        return new Data(payload.getData(), identifiers);
    }
}
