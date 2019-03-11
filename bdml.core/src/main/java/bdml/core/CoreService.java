package bdml.core;

import bdml.blockchain.BlockchainFacade;
import bdml.core.domain.*;
import bdml.core.domain.exceptions.AuthenticationException;
import bdml.core.domain.exceptions.DataUnavailableException;
import bdml.core.helper.Assert;
import bdml.core.helper.Configuration;
import bdml.core.helper.Crypto;
import bdml.core.persistence.*;
import bdml.cryptostore.CryptoStoreAdapter;
import bdml.keyserver.KeyDecoder;
import bdml.keyserver.KeyServerAdapter;
import bdml.services.Blockchain;
import bdml.services.CryptographicStore;
import bdml.services.KeyServer;
import bdml.services.QueryResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.PublicKey;
import java.util.*;
import java.util.stream.Collectors;

public class CoreService implements Core{

    private static final Logger LOGGER = LogManager.getLogger(PersonalCoreService.class);
    private static final String APP_CONFIG = "application.properties";
    private static final String DEFAULT_CONFIG = "default.application.properties";
    private static final int VERSION = 1;

    static Properties configuration;
    static Blockchain blockchain;
    static KeyServer keyServer;
    static CryptographicStore cryptoStore;
    static Serializer serializer;

    static {
        configuration = Configuration.load(APP_CONFIG, DEFAULT_CONFIG);
        blockchain = new BlockchainFacade(configuration);
        keyServer = new KeyServerAdapter(configuration);
        cryptoStore = new CryptoStoreAdapter(configuration);
        serializer = new KryoSerializer();
    }

    public static CoreService getInstance() {
        return Holder.INSTANCE;
    }

    public PersonalCoreService getPersonalService(Account account) throws AuthenticationException {
        AuthenticatedAccount caller = authenticate(account);
        return new PersonalCoreService(caller, configuration);
    }

    @Override
    public QueryResult<Payload> getDataDirect(Capability cap) throws DataUnavailableException {
        Assert.requireNonNull(cap, "cap");
        QueryResult<Payload> payload = extractValidPayload(cap, getDocument(cap.getIdentifier(), true));
        if(payload == null) throw new DataUnavailableException(cap.getIdentifier());
        return payload;
    }

    @Override
    public QueryResult<Payload> getPublicData(DataIdentifier id) throws DataUnavailableException {
        Assert.requireNonNull(id, "id");
        QueryResult<Payload> payload = extractValidPublicPayload(id, getDocument(id, true));
        if(payload == null) throw new DataUnavailableException(id);
        return payload;
    }


    @Override
    public DataIdentifier publishData(Payload payload) {
        Assert.requireNonNull(payload, "payload");

        //check if a valid payload for this type was produced
        //checking this here instead onf data allows to rect on missing capabilities
        //todo: throw instead
        if(!payload.isValid()) {
            return null;
        }

        //without attackers around will only run once
        while (true) {
            //will use a different nonce each time
            byte[] serializedDoc = Crypto.salt(CoreService.serializer.serializePayload(payload));
            Capability capability = Capability.of(serializedDoc);
            if(CoreService.blockchain.storeDocument(capability.getIdentifier().toByteArray(), serializedDoc, false)){
                // resolve subjects to public keys that will be able to read the data
                return capability.getIdentifier();
            }
        }
    }

    static List<QueryResult<Document>> getDocument(DataIdentifier identifier, boolean includePending) {
        List<QueryResult<byte[]>> serializedDocs = blockchain.getDocument(identifier.toByteArray(), includePending);
        return serializedDocs.stream().map(CoreService::deserializeDoc).filter(Objects::nonNull).collect(Collectors.toList());
    }

    static QueryResult<Payload> extractValidPublicPayload(DataIdentifier identifier, List<QueryResult<Document>> docs) {
        for(QueryResult<Document> doc:docs) {
            QueryResult<Payload> payload = parseAndValidatePublicPayload(identifier,doc);
            if(payload != null) return payload;
        }
        return null;
    }

    static QueryResult<Payload> extractValidPayload(Capability capability, List<QueryResult<Document>> docs) {
        for(QueryResult<Document> doc:docs) {
            QueryResult<Payload> payload = parseAndValidatePayload(capability,doc);
            if(payload != null) return payload;
        }
        return null;
    }


    private static QueryResult<Document> deserializeDoc(QueryResult<byte[]> serializedDoc) {
        QueryResult<Document> doc;
        try {
            doc = new QueryResult<>(CoreService.serializer.deserializeDocument(serializedDoc.data), serializedDoc.inclusionTime, serializedDoc.plain);
        } catch (DeserializationException e) {
            LOGGER.error(String.format("Attempted to deserialize a malformed frame: %s", e.getMessage()));
            return null;
        }

        if (doc.data.getVersion() != VERSION) {
            LOGGER.error(String.format("Attempted to deserialize a frame of version: %s, expected: %s", doc.data.getVersion(), VERSION));
            return null;
        }

        return doc;
    }

    private static QueryResult<Payload> parseAndValidatePayload(Capability capability, QueryResult<Document> doc) {
        try {
            byte[] decPayload;
            if(doc.plain) {
                decPayload = Crypto.unSalt(doc.data.getPayload());
            } else {
                decPayload = Crypto.selfDecrypt(capability, doc.data.getPayload());
            }

            if(decPayload == null) {
                LOGGER.error(String.format("Failed to validate the payload of frame '%s'", capability.getIdentifier().toString()));
                return null;
            }
            return new QueryResult<>(validatePayload(capability.getIdentifier(), decPayload),doc.inclusionTime,true);
        } catch (IllegalArgumentException e) {
            LOGGER.error(String.format("Failed to decrypt the payload of frame '%s': %s", capability.getIdentifier().toString(), e.getMessage()));
            return null;
        }
    }

    private static QueryResult<Payload> parseAndValidatePublicPayload(DataIdentifier identifier, QueryResult<Document> doc) {
        try {
            byte[] decPayload;
            if(doc.plain) {
                decPayload = Crypto.unSalt(doc.data.getPayload());
            } else {
                LOGGER.error(String.format("Failed to decrypt the payload of frame '%s'", identifier.toString()));
                return null;
            }
            return new QueryResult<>(validatePayload(identifier, decPayload),doc.inclusionTime,true);
        } catch (IllegalArgumentException e) {
            LOGGER.error(String.format("Failed to decrypt the payload of frame '%s': %s", identifier.toString(), e.getMessage()));
            return null;
        }
    }

    private static Payload validatePayload(DataIdentifier id, byte[] decPayload){
        try {
            Payload payload = CoreService.serializer.deserializePayload(decPayload);
            if(payload == null || !payload.isValid()){
                LOGGER.error(String.format("Failed to validate the payload of frame '%s'", id.toString()));
                return null;
            }
            return payload;
        } catch (DeserializationException e) {
            LOGGER.error(String.format("Attempted to deserialize the payload of frame '%s': %s", id.toString(), e.getMessage()));
            return null;
        }

    }

    /**
     * Authenticates whether the given account exists.
     *
     * @param account identifier and password combination
     * @return {@link AuthenticatedAccount} object containing the account with its public key.
     * @throws AuthenticationException if the given identifier and password combination was wrong or does not exist.
     */
    private static AuthenticatedAccount authenticate(Account account) throws AuthenticationException {
        Assert.requireNonNull(account, "account");

        // check whether account exists
        PublicKey publicKey = keyServer.queryKey(account.getIdentifier());

        // check whether the resolved public key and given password correspond to an account
        if (!cryptoStore.checkKeyPair(publicKey, account.getPassword()))
            throw new AuthenticationException();

        return new AuthenticatedAccount(account, publicKey);
    }

    //region PersonalCore interface implementation
    //------------------------------------------------------------------------------------------------------------------
    @Override
    public Subject createAccount(String password) {
        Assert.requireNonEmpty(password, "password");

        PublicKey publicKey = cryptoStore.generateKeyPair(password);

        Subject subject = Subject.deriveFrom(publicKey);
        Account account = new Account(subject, password);

        keyServer.registerKey(account.getIdentifier(), publicKey);

        return subject;
    }


    @Override
    public String exportSubjectKey(Subject subject) {
        PublicKey key = keyServer.queryKey(subject.toString());
        if(key == null) return null;
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    @Override
    public Subject importSubject(String pk) {
        PublicKey key = KeyDecoder.decodePublicKey(pk);
        Subject subject = Subject.deriveFrom(key);
        keyServer.registerKey(subject.toString(),key);
        return subject;
    }

    @Override
    public Map.Entry<Capability, byte[]> selfEncrypt(byte[] data){
        return Crypto.selfEncrypt(data);
    }

    @Override
    public byte[] selfDecrypt(Capability key, byte[] data) {
        return Crypto.selfDecrypt(key,data);
    }


    /**
     * Queries the key server for the public key of the given subject.
     *
     * @param subject {@link Subject}
     * @return {@link PublicKey} corresponding to {@code subject}.
     * @throws IllegalArgumentException if the given {@code subject} is unknown to the key server.
     */
    static PublicKey queryPublicKey(Subject subject) {
        PublicKey pk = keyServer.queryKey(subject.toString());

        return Optional.ofNullable(pk)
                .orElseThrow(() -> new IllegalArgumentException(String.format("There was no subject found for the identifier '%s'.", subject.toString())));
    }

    @Override
    public boolean addUpdateListener(Blockchain.BlockFinalizedListener listener) {
        return blockchain.addBlockListener(listener);
    }

    @Override
    public boolean removeUpdateListener(Blockchain.BlockFinalizedListener listener) {
        return blockchain.removeBlockListener(listener);
    }

    // initialize-on-demand holder
    private static class Holder {
        private static final CoreService INSTANCE = new CoreService();
    }

}
