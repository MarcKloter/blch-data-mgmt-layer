package bdml.core;

import java.io.ByteArrayOutputStream;
import java.security.PublicKey;
import java.util.*;
import java.util.stream.Collectors;

import bdml.blockchain.BlockchainFacade;
import bdml.cache.CacheImpl;
import bdml.core.helper.Assert;
import bdml.core.helper.Crypto;
import bdml.core.helper.StorageObject;
import bdml.cryptostore.CryptoStoreAdapter;
import bdml.keyserver.KeyServerAdapter;
import bdml.services.Blockchain;
import bdml.services.Cache;
import bdml.services.CryptographicStore;
import bdml.services.KeyServer;
import bdml.services.api.exceptions.AuthenticationException;
import bdml.services.api.exceptions.NotAuthorizedException;
import bdml.services.api.types.Account;
import bdml.services.api.types.Filter;
import bdml.services.api.Core;
import org.bouncycastle.util.encoders.Hex;

public class CoreService implements Core {
    // TODO: load constants from configuration file
    private final byte VERSION = 1;

    private Blockchain blockchain = new BlockchainFacade();
    private KeyServer keyServer = new KeyServerAdapter();
    private CryptographicStore cryptoStore = new CryptoStoreAdapter();
    private Cache cache = new CacheImpl();

    @Override
    public String storeData(String data, Account account, List<String> subjects, List<String> attachments) throws AuthenticationException {
        // validate input
        Assert.requireNonEmpty(data, "data");
        Assert.requireNonNull(account, "account");

        // check whether account exists
        PublicKey ownerPK = keyServer.queryKey(account.getIdentifier());

        // check whether the resolved public key and given password correspond to an account
        if (!cryptoStore.checkKeyPair(ownerPK, account.getPassword()))
            throw new AuthenticationException();

        // resolve subjects to public keys that will be able to read the data
        Set<PublicKey> recipients = new HashSet<>();
        if (subjects != null)
            subjects.stream()
                    .distinct() // distinct to avoid unnecessary operations
                    .map(this::resolveSubject)
                    .collect(Collectors.toCollection(() -> recipients));

        // add owner as recipient
        recipients.add(ownerPK);

        // check whether all data (capabilities) to be attached exist and can be access by the given account
        Set<byte[]> attachedCapabilities = new HashSet<>();
        if (attachments != null)
            attachments.stream()
                    .distinct() // distinct to avoid unnecessary operations
                    .map(Hex::decode) // decode data identifiers
                    .map(id -> getCapability(account, id))
                    .collect(Collectors.toCollection(() -> attachedCapabilities));

        // TODO: (optional) save data to IPFS

        StorageObject storageObject = assembleData(data.getBytes(), recipients, attachedCapabilities);

        // persist data in blockchain
        blockchain.createTransaction(account, storageObject.getIdentifier(), storageObject.getData());

        // cache capability
        cache.add(account, storageObject.getIdentifier(), storageObject.getCapability());

        return Hex.toHexString(storageObject.getIdentifier());
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

    /**
     * Resolves the given data identifier to the capability used to encrypt the associated data.
     * The identifier is first being looked up in the cache and retrieved from the blockchain if not found.
     *
     * @param account    account to access the capability as
     * @param identifier data identifier
     * @return Capability as byte array.
     * @throws IllegalArgumentException if there is no data linked to the given identifier
     * @throws NotAuthorizedException   if the data linked to the given identifier could not be accessed by the provided account
     */
    private byte[] getCapability(Account account, byte[] identifier) {
        // check whether the capability to be included is cached
        byte[] capability = cache.get(account, identifier);

        // if the data is not present within the cache, retrieve it from the blockchain
        if (capability == null) {
            byte[] data = blockchain.getTransaction(identifier);

            if (data == null)
                throw new IllegalArgumentException(String.format("There was no data found identified by '%s'.", Hex.toHexString(identifier)));

            // TODO: get cap from disasseble, maybe cache it here?
            capability = disassembleData(account, data);

            if (capability == null)
                throw new NotAuthorizedException("The data to be attached could not be accessed or does not exist.");
        }

        return capability;
    }

    /**
     * Prepares the given data for storing.
     *
     * @param data
     * @param recipients
     * @param attachedCapabilities
     * @return
     */
    private StorageObject assembleData(byte[] data, Set<PublicKey> recipients, Set<byte[]> attachedCapabilities) {
        // TODO: refactor this method
        byte[] capability = Crypto.hashValue(data);
        byte[] identifier = Crypto.hashValue(capability);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // build header: for each recipient [1 byte unsigned size indicator || encrypted capability]
        for (PublicKey recipient : recipients) {
            byte[] encCap = cryptoStore.encrypt(recipient, capability);

            if (encCap.length > 0xff)
                throw new IllegalStateException("The encrypted capability exceeds the size limit of 255 bytes.");

            byte encCapSize = (byte) encCap.length;
            outputStream.write(encCapSize);
            outputStream.writeBytes(encCap);
        }

        byte[] header = outputStream.toByteArray();
        outputStream.reset();

        // 2 bytes unsigned header size indicator
        if(header.length > 0xffff)
            throw new IllegalStateException("The composed header exceeds the size limit of 65535 bytes.");

        short headerSize = (short) header.length;

        // concatenate all capabilities to attach
        for (byte[] attachedCapability : attachedCapabilities)
            outputStream.writeBytes(attachedCapability);

        byte[] attachment = outputStream.toByteArray();
        outputStream.reset();

        if(attachment.length > 0xffff)
            throw new IllegalStateException("The capabilities to attach exceed the size limit of 65535 bytes.");

        short attachmentSize = (short) attachment.length;

        // arrange payload: size of attachment || data || attachment
        outputStream.write(attachmentSize);
        outputStream.writeBytes(data);
        outputStream.writeBytes(attachment);

        // encrypt payload
        byte[] payload = Crypto.symmetricallyEncrypt(capability, outputStream.toByteArray());
        outputStream.reset();

        // VERSION || size of header || header || payload
        outputStream.write(VERSION);
        outputStream.write(headerSize);
        outputStream.writeBytes(header);
        outputStream.writeBytes(payload);

        return new StorageObject(identifier, capability, outputStream.toByteArray());
    }

    private byte[] disassembleData(Account account, byte[] data) {
        // TODO: split E_PK1(cap) E_PK2(cap) ...
        // signed byte to unsigned number: int i = (0x000000FF) & b;
        return null;
    }

    @Override
    public List<String> listData(Account account, Filter filter) throws AuthenticationException {
        // validate input
        Objects.requireNonNull(account, "Parameter 'account' cannot be null.");

        // TODO: implement
        if (false)
            throw new AuthenticationException();
        return null;
    }

    @Override
    public String getData(String id, Account account) throws AuthenticationException {
        // validate input
        Assert.requireNonEmpty(id, "id");
        Assert.requireNonNull(account, "account");

        // TODO: implement
        if (false)
            throw new AuthenticationException();
        return null;
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
        // validate input
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

        return identifier;
    }

}
