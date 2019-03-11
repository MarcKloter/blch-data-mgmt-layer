package bdml.core;

import java.security.PublicKey;
import java.util.*;
import java.util.List;

import bdml.core.cache.CacheImpl;
import bdml.core.domain.exceptions.*;
import bdml.core.helper.*;
import bdml.core.persistence.*;
import bdml.core.domain.*;
import bdml.services.*;
import bdml.core.cache.PersonalCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class PersonalCoreService implements PersonalCore {
    private static final int VERSION = 1;

    private static final Logger LOGGER = LogManager.getLogger(PersonalCoreService.class);
    private final PersonalCache cache;
    private final AuthenticatedAccount account;


    PersonalCoreService(AuthenticatedAccount account, Properties configuration) {
        this.account = account;
        this.cache = new CacheImpl(account, configuration);
    }

    @Override
    public Subject getActiveAccount() {
        return account.getSubject();
    }

    @Override
    public DataIdentifier storeData(Data data) {
        Assert.requireNonNull(data, "data");

        //lookup the capability and transform data into payload
        //The false eis for integrity else someone can store a link to an attachment that will never exist
        Payload payload =  data.resolveAttachments(id -> lookupCapability(id, false));

        //check if a valid payload for this type was produced
        //checking this here instead onf data allows to rect on missing capabilities
        //todo: throw instead
        if(payload == null || !payload.isValid()) {
            return null;
        }


        //without attackers around will only run once
        while (true) {
            //will use a different nonce each time
            AccessibleDocument doc = assembleDocument(payload);
            byte[] serializedDoc = CoreService.serializer.serializeDocument(doc.getDocument());

            if(CoreService.blockchain.storeDocument(doc.getIdentifier().toByteArray(), serializedDoc, true)){
                cache.addCapability(doc.getCapability(), true);
                return doc.getIdentifier();
            }
        }

    }

    private void generateAccessToken(Capability cap, Subject subject){
        AccessToken acc = assembleAccessToken(cap,subject);
        CoreService.blockchain.storeAccessToken(acc.getId(), acc.getEncryptedCapability());
    }

    @Override
    public void grantAccess(DataIdentifier id, Subject subject) throws DataUnavailableException {
        Assert.requireNonNull(id, "id");
        Assert.requireNonNull(subject, "subject");
        Capability cap = lookupCapability(id, false);
        if(cap == null) throw new DataUnavailableException(id);
        generateAccessToken(cap,subject);
    }


    private void generateAmendment(Capability capOrig, Capability capNew){
        AccessToken acc = assembleCapabilityToken(capNew,capOrig);
        CoreService.blockchain.storeAmendmentToken(acc.getId(),acc.getEncryptedCapability());
    }

    @Override
    public void amendDocument(DataIdentifier original, DataIdentifier amendment) throws DataUnavailableException {
        Assert.requireNonNull(original, "original");
        Assert.requireNonNull(amendment, "amendment");
        Capability capOrig = lookupCapability(original, false);
        if(capOrig == null) throw new DataUnavailableException(original);
        Capability capNew = lookupCapability(amendment, false);
        if(capNew == null) throw new DataUnavailableException(amendment);
        generateAmendment(capOrig,capNew);
    }


    @Override
    public DataIdentifier publishData(Data data) {
        Assert.requireNonNull(data, "data");
        //lookup the capability and transform data into payload
        Payload payload =  data.resolveAttachments(id -> lookupCapability(id, false));

        //check if a valid payload for this type was produced
        //checking this here instead onf data allows to rect on missing capabilities
        //todo: throw instead
        if(payload == null || !payload.isValid()) {
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



    @Override
    public Set<DataIdentifier> listAmendmentsToData(DataIdentifier identifier) {
        Assert.requireNonNull(identifier, "identifier");
        updateIndexes();
        return cache.getLink(identifier, true);
    }

    @Override
    public Set<DataIdentifier> listAttachmentsToData(DataIdentifier identifier) {
        Assert.requireNonNull(identifier, "identifier");
        updateIndexes();
        return cache.getLink(identifier, false);
    }

    @Override
    public Set<DataIdentifier> listNewDocuments() {
        updateIndexes();
        return cache.getNewIdentifiers();
    }


    @Override
    public QueryResult<Data> getData(DataIdentifier identifier) throws DataUnavailableException {
        Assert.requireNonNull(identifier, "identifier");
        updateIndexes();
        QueryResult<ValidDocument> frame = extractValidDocument(identifier, CoreService.getDocument(identifier, true));
        if(frame == null || frame.data == null) throw new DataUnavailableException(identifier);
        return new QueryResult<>(frame.data.getPayload().processCapabilities(Capability::getIdentifier),frame.inclusionTime,frame.plain);
    }


    //------------------------------------------------------------------------------------------------------------------
    //endregion


    private Capability lookupCapability(DataIdentifier id, boolean includePending) {
        //todo: can we hit cache??? or other shortcut???
        QueryResult<ValidDocument> frame = extractValidDocument(id, CoreService.getDocument(id, includePending));
        if(frame == null || frame.data == null) return null;
        return frame.data.getCapability();
    }



    private AccessToken assembleAccessToken(Capability capability, Subject subject) {
        PublicKey recipient = queryPublicKey(subject);

        byte[] encryptedCapability = CoreService.cryptoStore.encrypt(recipient, capability.toByteArray());
        //Make a t a better place
        byte[] ident = subject.toBytes();
        return new AccessToken(VERSION, ident, encryptedCapability);
    }

    private AccessToken assembleCapabilityToken(Capability capability, Capability subject) {
        byte[] encryptedCapability = Crypto.capabilityEncrypt(subject, capability.toByteArray());
        //Make a t a better place
        byte[] ident = subject.toByteArray();
        return new AccessToken(VERSION, ident, encryptedCapability);
    }

    /**
     * Envelops the given data into a frame for storing.
     *
     * @param payload {@link Payload} to serialize
     * @return {@link AccessibleDocument} containing the capability and {@link Document}
     */
    private AccessibleDocument assembleDocument(Payload payload) {
        Map.Entry<Capability, byte[]> res = Crypto.selfEncrypt(CoreService.serializer.serializePayload(payload));
        return new AccessibleDocument(new Document(VERSION,res.getValue()), res.getKey());
    }


    private QueryResult<ValidDocument> extractValidDocument(DataIdentifier identifier, List<QueryResult<Document>> docs) {
        Optional<Capability> capability = cache.getCapability(identifier);
        if(capability.isEmpty()) return null;
        QueryResult<Payload> payload = CoreService.extractValidPayload(capability.get(), docs);
        if(payload == null || payload.data == null) return null;
        return new QueryResult<>(new ValidDocument(payload.data,capability.get()),payload.inclusionTime,true);
    }


    /**
     * Attempts to decrypt a capability matching the provided identifier from a list of ciphertexts using the given account.
     *
     * @param token to extract the capability from
     * @return {@link Optional} containing the {@link Capability} retrieved from {@code token} or {@link Optional#empty()}
     */
    private Optional<Capability> decryptAccessToken(byte[] token) {
        byte[] plain = CoreService.cryptoStore.decrypt(account.getPublicKey(), account.getPassword(), token);
        try {
            return Optional.of(new Capability(plain));
        }
        catch (CapabilityFormatException e) {
            return Optional.empty();
        }
    }

    private Optional<Capability> decryptAmendmentToken(Capability cap, byte[] token) {
        byte[] plain = Crypto.capabilityDecrypt(cap,token);
        try {
            return Optional.of(new Capability(plain));
        }
        catch (CapabilityFormatException e) {
            return Optional.empty();
        }
    }

    private void processRecursively(Capability capability){
        Queue<Capability> recursive = new LinkedList<>();
        recursive.offer(capability);

        while (!recursive.isEmpty()) {
            Capability cap = recursive.poll();
            if(cache.makePermanentIfExists(cap.getIdentifier()).orElse(false)) continue;
            //todo: check if somebody listens on this data: if yes -- we need to call teh callback after processRecursively finished

            QueryResult<Payload> payload = CoreService.extractValidPayload(cap, CoreService.getDocument(cap.getIdentifier(), false));
            if(payload == null || payload.data == null) continue;
            cache.addCapability(cap, false);
            payload.data.processCapabilities(linkCap -> {
                recursive.offer(linkCap);
                cache.addLink(cap.getIdentifier(), linkCap.getIdentifier(), false);
                return linkCap.getIdentifier();
            });
            List<QueryResult<byte[]>> amendments = CoreService.blockchain.getAllAmendmentTokensFor(cap.getIdentifier().toByteArray());
            for(QueryResult<byte[]> amend :amendments) {
                decryptAmendmentToken(cap, amend.data).ifPresent(amendCap -> {
                    recursive.offer(amendCap);
                    cache.addLink(cap.getIdentifier(), amendCap.getIdentifier(), true);
                });
            }

        }
    }

    private void updateIndexes() {
        long currentBlock = CoreService.blockchain.blockPointer();
        long nextBlock =  cache.getPointer();
        if(nextBlock > currentBlock) return;

        List<QueryResult<byte[]>> plainDocs = CoreService.blockchain.getAllPlainIds(nextBlock, currentBlock+1);
        for (QueryResult<byte[]> docId : plainDocs) {
            DataIdentifier identifier = new DataIdentifier(docId.data);
            QueryResult<ValidDocument> frame = extractValidDocument(identifier, CoreService.getDocument(identifier, false));
            if(frame == null || frame.data == null) continue;
            processRecursively(frame.data.getCapability());
        }

        List<QueryResult<byte[]>> newTokens = CoreService.blockchain.getAllTokens(nextBlock, currentBlock+1, account.getSubject().toBytes());
        for (QueryResult<byte[]> rawToken : newTokens) {
            Optional<Capability> cap = decryptAccessToken(rawToken.data);
            if(cap.isEmpty()) continue;
            processRecursively(cap.get());
        }

        List<QueryResult<Pair<byte[],byte[]>>> newAmendments = CoreService.blockchain.getAllAmendmentTokens(nextBlock, currentBlock+1);
        for(QueryResult<Pair<byte[],byte[]>> rawToken : newAmendments) {
            Optional<Capability> source = cache.getCapability(new DataIdentifier(rawToken.data.first));
            if(source.isEmpty()) continue;
            Optional<Capability> cap = decryptAmendmentToken(source.get(), rawToken.data.second);
            if(cap.isEmpty()) continue;
            cache.addLink(source.get().getIdentifier(), cap.get().getIdentifier(), true);
            processRecursively(cap.get());
        }

        cache.setPointer(currentBlock+1);
    }

    private PublicKey queryPublicKey(Subject subject) {
        PublicKey pk = CoreService.keyServer.queryKey(subject.toString());

        return Optional.ofNullable(pk)
                .orElseThrow(() -> new IllegalArgumentException(String.format("There was no subject found for the identifier '%s'.", subject.toString())));
    }


}
