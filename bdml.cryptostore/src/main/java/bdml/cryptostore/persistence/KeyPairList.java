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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class KeyPairList {
    private static final String FILENAME = "keyPairList.json";

    private final String filepath;

    private List<SecuredKeyPair> mapping;

    public KeyPairList(String outputDirectory) {
        this.filepath = outputDirectory;

        // load previously generated key pairs
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(filepath, FILENAME);
        if (file.exists()) {
            try {
                JsonParser jsonParser = new JsonFactory().createParser(file);
                CollectionType valueType = mapper.getTypeFactory().constructCollectionType(List.class, SecuredKeyPair.class);
                this.mapping = mapper.readValue(jsonParser, valueType);
            } catch (IOException e) {
                throw new MisconfigurationException(e.getMessage());
            }
        } else {
            // create path if it doesn't exist
            Path path = Paths.get(filepath);
            if(Files.notExists(path)) {
                try {
                    Files.createDirectories(path);
                } catch (IOException e) {
                    throw new MisconfigurationException(e.getMessage());
                }
            }

            this.mapping = new ArrayList<>();
        }
    }

    /**
     * Appends the specified key pair to the end of the persisted list.
     *
     * @param keyPair  key pair to append to the list
     * @param password Password to secure the key pair.
     */
    public void add(KeyPair keyPair, String password) {
        String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        String privateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
        String passwordHash = Util.sha256(password);
        this.mapping.add(new SecuredKeyPair(publicKey, privateKey, passwordHash));

        // write list to file
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(new FileWriter(new File(filepath, FILENAME), false), mapping);
        } catch (IOException e) {
            throw new MisconfigurationException(e.getMessage());
        }
    }

    /**
     * Searches the list of key pairs for a given public key secured by the provided secret.
     * Returns the private key of the found key pair.
     *
     * @param key    public key to look for
     * @param secret password to the given public key
     * @return Bytes of private key corresponding to the given public key/secret combination or an empty byte array.
     */
    public byte[] get(PublicKey key, String secret) {
        if (key == null || secret == null)
            return new byte[0];

        String encodedPublicKey = Base64.getEncoder().encodeToString(key.getEncoded());
        String passwordHash = Util.sha256(secret);

        return mapping.stream()
                .filter(o -> o.getPublicKey().equals(encodedPublicKey) && o.getPasswordHash().equals(passwordHash))
                .findFirst()
                .map(encodedPrivateKey -> Base64.getDecoder().decode(encodedPrivateKey.getPrivateKey()))
                .orElse(null);
    }
}
