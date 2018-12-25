package bdml.cryptostore;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import bdml.cryptostore.persistence.SecuredKeyPair;
import bdml.services.CryptographicStore;
import bdml.services.exceptions.MisconfigurationException;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

public class CryptoStoreAdapter implements CryptographicStore {
    // TODO: load KEY_GEN_ALG, EC_CURVE and FILENAME from configuration file
    private final String KEY_GEN_ALG = "EC";
    private final String EC_CURVE = "secp256k1";
    private final String FILENAME = "keyPairs.json";

    private List<SecuredKeyPair> keyPairs;

    public CryptoStoreAdapter() {
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

    @Override
    public PublicKey generateKeyPair(String secret) {
        // generate key pair
        KeyPairGenerator gen;
        try {
            gen = KeyPairGenerator.getInstance(KEY_GEN_ALG);
        } catch (NoSuchAlgorithmException e) {
            throw new MisconfigurationException(e.getMessage());
        }

        if (KEY_GEN_ALG.equals("EC")) {
            ECGenParameterSpec spec = new ECGenParameterSpec(EC_CURVE);
            try {
                gen.initialize(spec);
            } catch (InvalidAlgorithmParameterException e) {
                throw new MisconfigurationException(e.getMessage());
            }
        }
        KeyPair keyPair = gen.generateKeyPair();

        // persist generated key pair
        String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        String privateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
        String passwordHash = Util.sha256(secret);
        this.keyPairs.add(new SecuredKeyPair(publicKey, privateKey, passwordHash));

        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(new FileWriter(FILENAME, false), keyPairs);
        } catch (IOException e) {
            throw new MisconfigurationException(e.getMessage());
        }

        return keyPair.getPublic();
    }

    @Override
    public boolean checkKeyPair(PublicKey key, String secret) {
        return get(key, secret) != null;
    }

    @Override
    public String encrypt(PublicKey key, String plaintext) {
        // ECIESwithAES
        return null;
    }

    @Override
    public String decrypt(PublicKey key, String secret, String ciphertext) {
        // ECIESwithAES
        return null;
    }

    /**
     * Searches the list of key pairs for a given public key secured by the provided secret.
     *
     * @param key    public key to look for
     * @param secret password to the given public key
     * @return Public key if the combination was found or null.
     */
    private SecuredKeyPair get(PublicKey key, String secret) {
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
