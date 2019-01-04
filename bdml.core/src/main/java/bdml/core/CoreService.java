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
import org.bouncycastle.util.encoders.Hex;

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

        // resolve subjects to public keys that will be able to read the data
        Set<PublicKey> recipients = resolveAllSubjects(subjects);

        // add owner as recipient
        recipients.add(caller.getPublicKey());

        // resolve data identifiers to capabilities using the given account (requires access to the data)
        Set<byte[]> attachedCapabilities = resolveAllIdentifiers(caller, attachments);

        // TODO: save data to IPFS

        FrameWrapper frame = assembleFrame(data, recipients, attachedCapabilities);

        // persist frame in blockchain
        blockchain.createTransaction(account, frame.getIdentifier(), frame.getBytes());

        // cache capability
        cache.add(account, frame.getIdentifier(), frame.getCapability());

        return Hex.toHexString(frame.getIdentifier());
    }

    @Override
    public String storeData(Data data, Account account, List<String> subjects) throws AuthenticationException {
        return storeData(data.getData(), data.getAttachments(), account, subjects);
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
        byte[] idBytes = Hex.decode(identifier);
        Assert.require32Bytes(idBytes);

        // check provided account
        KnownAccount caller = checkAccount(account);

        // retrieve data frame from blockchain
        byte[] frame = blockchain.getTransaction(idBytes);

        FrameWrapper wrappedFrame = disassembleFrame(caller, Hex.decode(identifier), frame);

        if (wrappedFrame == null)
            return null;

        return decryptPayload(wrappedFrame, account);
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
        String identifier = Hex.toHexString(idBytes);

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
     * Resolves the given addresses to public keys using the keyserver.
     *
     * @param subjects list of addresses
     * @return Set of distinct public keys corresponding to the given subjects.
     */
    private Set<PublicKey> resolveAllSubjects(List<String> subjects) {
        Set<PublicKey> resolvedSubjects = new HashSet<>();
        if (subjects != null)
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

        if (pk == null)
            throw new IllegalArgumentException(String.format("There was no subject found for the identifier '%s'.", identifier));

        return pk;
    }

    private Set<byte[]> resolveAllIdentifiers(KnownAccount account, Collection<String> identifiers) {
        Set<byte[]> capabilities = new HashSet<>();
        if (identifiers != null)
            identifiers.stream()
                    .distinct() // distinct to avoid unnecessary operations
                    .map(id -> resolveIdentifier(account, id))
                    .collect(Collectors.toCollection(() -> capabilities));
        return capabilities;
    }

    private byte[] resolveIdentifier(KnownAccount account, String idHex) {
        byte[] idBytes = Hex.decode(idHex);
        Assert.require32Bytes(idBytes);

        // check cache for capability
        byte[] capability = cache.get(account, idBytes);

        // if the capability was not found in the cache, retrieve the corresponding data from the blockchain
        if (capability == null) {
            byte[] frame = blockchain.getTransaction(idBytes);
            FrameOuterClass.Frame protoFrame = Protobuf.parseFrame(frame, VERSION);

            if (frame == null)
                throw new IllegalArgumentException(String.format("There was no data found identified by '%s'.", idHex));

            capability = decryptCapability(account, protoFrame, idBytes);

            if (capability == null)
                throw new NotAuthorizedException("The data to be attached could not be accessed.");
        }

        return capability;
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
        // CAP = H(DATA)
        byte[] capability = Crypto.hashValue(data);

        // ID = H(CAP)
        byte[] identifier = Crypto.hashValue(capability);

        // encrypt CAP for each recipient PK
        List<byte[]> encryptedCapability = encryptCapability(capability, recipients);

        // build the unencrypted payload: data || attachedCapabilities
        byte[] payload = Protobuf.buildPayload(data, attachedCapabilities);

        // symmetrically encrypt payload using the capability
        byte[] encryptedPayload = Crypto.symmetricallyEncrypt(capability, payload);

        FrameOuterClass.Frame frame = Protobuf.buildFrame(VERSION, encryptedCapability, encryptedPayload);

        return new FrameWrapper(identifier, capability, frame);
    }

    /**
     * Takes apart the given protocol buffer frame message.
     *
     * @param account
     * @param identifier
     * @param frame
     * @return
     */
    private FrameWrapper disassembleFrame(KnownAccount account, byte[] identifier, byte[] frame) {
        // get protocol buffer frame message object for the configured version
        FrameOuterClass.Frame protoFrame = Protobuf.parseFrame(frame, VERSION);

        // check cache for capability, decrypt it from the frame otherwise
        byte[] capability = Optional.ofNullable(cache.get(account, identifier))
                .orElse(decryptCapability(account, protoFrame, identifier));

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

    private Data decryptPayload(FrameWrapper wrappedFrame, Account account) {
        // retrieve encrypted payload
        byte[] encPayload = Protobuf.decode(wrappedFrame.unwrap().getEncryptedPayload());
        // decrypt the payload using the capability
        byte[] decPayload = Crypto.symmetricallyDecrypt(wrappedFrame.getCapability(), encPayload);
        // get protocol buffer payload message object
        FrameOuterClass.Payload payload = Protobuf.parsePayload(decPayload);

        List<byte[]> capabilities = Protobuf.decode(payload.getAttachedCapabilityList());
        List<String> identifiers = capabilities.stream().map(Crypto::hashValue).map(Hex::toHexString).collect(Collectors.toList());

        // cache attached capabilities
        capabilities.forEach(cap -> cache.add(account, Crypto.hashValue(cap), cap));

        return new Data(payload.getData(), identifiers);
    }
}
