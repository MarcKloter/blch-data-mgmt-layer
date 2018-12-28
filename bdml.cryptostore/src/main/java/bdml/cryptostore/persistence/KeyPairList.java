package bdml.cryptostore.persistence;

import bdml.cryptostore.Util;
import bdml.services.exceptions.MisconfigurationException;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class KeyPairList {
    // TODO: load FILENAME from configuration file
    private final String FILENAME = "keyPairList.json";

    private List<SecuredKeyPair> keyPairs;

    public KeyPairList() {
        // load previously generated key pairs
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(FILENAME);
        if (file.exists()) {
            try {
                JsonParser jsonParser = new JsonFactory().createParser(file);
                CollectionType valueType = mapper.getTypeFactory().constructCollectionType(List.class, SecuredKeyPair.class);
                this.keyPairs = mapper.readValue(jsonParser, valueType);
            } catch (IOException e) {
                throw new MisconfigurationException(e.getMessage());
            }
        } else {
            this.keyPairs = new ArrayList<>();
        }
    }

    /**
     * Appends the specified key pair to the end of the persisted list.
     *
     * @param keyPair key pair to append to the list
     * @param password Password to secure the key pair.
     */
    public void add(KeyPair keyPair, String password) {
        String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        String privateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
        String passwordHash = Util.sha256(password);
        this.keyPairs.add(new SecuredKeyPair(publicKey, privateKey, passwordHash));

        // write list to file
        ObjectMapper mapper = new ObjectMapper();
            try {
            mapper.writeValue(new FileWriter(FILENAME, false), keyPairs);
        } catch (IOException e) {
            throw new MisconfigurationException(e.getMessage());
        }
    }

    /**
     * Searches the list of key pairs for a given public key secured by the provided secret.
     *
     * @param key    public key to look for
     * @param secret password to the given public key
     * @return Public key if the combination was found or null.
     */
    public SecuredKeyPair get(PublicKey key, String secret) {
        if (key == null || secret == null)
            return null;

        String encodedKey = Base64.getEncoder().encodeToString(key.getEncoded());
        String passwordHash = Util.sha256(secret);
        return keyPairs.stream()
                .filter(o -> o.getPublicKey().equals(encodedKey) && o.getPasswordHash().equals(passwordHash))
                .findFirst()
                .orElse(null);
    }
}
