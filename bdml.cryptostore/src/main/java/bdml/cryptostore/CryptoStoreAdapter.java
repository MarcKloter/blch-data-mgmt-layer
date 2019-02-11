package bdml.cryptostore;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Properties;

import bdml.cryptostore.persistence.KeyPairList;
import bdml.services.CryptographicStore;
import bdml.services.exceptions.MisconfigurationException;
import bdml.services.exceptions.MissingConfigurationException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.IESParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;

public class CryptoStoreAdapter implements CryptographicStore {
    // mandatory configuration properties
    private static final String OUTPUT_DIRECTORY_KEY = "bdml.output.directory";

    private static final String KEY_GEN_ALG = "EC";
    private static final String ASYMMETRIC_CIPHER = "ECIESwithAES-CBC";
    private static final String EC_CURVE = "secp256k1"; // other curves: secp521r1, secp384r1

    private final KeyPairList keyPairs;

    public CryptoStoreAdapter(Properties configuration) {
        //load configuration
        String outputDirectory = getProperty(configuration, OUTPUT_DIRECTORY_KEY);

        this.keyPairs = new KeyPairList(outputDirectory);
    }

    private String getProperty(Properties configuration, String property) {
        if(!configuration.containsKey(property))
            throw new MissingConfigurationException(property);

        return configuration.getProperty(property);
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

        // set the curve for ECC
        ECGenParameterSpec spec = new ECGenParameterSpec(EC_CURVE);
        try {
            gen.initialize(spec);
        } catch (InvalidAlgorithmParameterException e) {
            throw new MisconfigurationException(e.getMessage());
        }
        KeyPair keyPair = gen.generateKeyPair();

        // persist generated key pair
        this.keyPairs.add(keyPair, secret);

        return keyPair.getPublic();
    }

    @Override
    public boolean checkKeyPair(PublicKey key, String secret) {
        return keyPairs.get(key, secret) != null;
    }

    @Override
    public byte[] encrypt(PublicKey key, byte[] plaintext) {
        Security.addProvider(new BouncyCastleProvider());

        // generate nonce and enable EC point compression
        byte[] nonce = new byte[16];
        new SecureRandom().nextBytes(nonce);
        IESParameterSpec params = new IESParameterSpec(null, null, 256, 256, nonce, true);

        // encrypt plaintext using the configured cipher
        try {
            Cipher iesCipher = Cipher.getInstance(ASYMMETRIC_CIPHER, "BC");
            iesCipher.init(Cipher.ENCRYPT_MODE, key, params);

            // format: 0x02/0x03 || coordinate x || (PKCS5 padded) ciphertext || 20 bytes HMAC-digest || 16 bytes nonce
            return Util.concatenate(iesCipher.doFinal(plaintext), nonce);
        } catch (GeneralSecurityException e) {
            throw new MisconfigurationException(e.getMessage());
        }
    }

    @Override
    public byte[] decrypt(PublicKey publicKey, String secret, byte[] ciphertext) {
        Security.addProvider(new BouncyCastleProvider());

        // getCapability private key persisted for the given public key and secret combination
        byte[] decodedKey = keyPairs.get(publicKey, secret);
        if(decodedKey == null) return null;

        // split ciphertext into ciphertext and nonce
        int index = ciphertext.length - 16;
        byte[] nonce = Arrays.copyOfRange(ciphertext, index, ciphertext.length);
        ciphertext = Arrays.copyOf(ciphertext, index);
        IESParameterSpec params = new IESParameterSpec(null, null, 256, 256, nonce, true);

        try {
            // reconstruct the encoded X.509 formatted private key from bytes
            PKCS8EncodedKeySpec x509KeySpec = new PKCS8EncodedKeySpec(decodedKey);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_GEN_ALG);
            PrivateKey privateKey = keyFactory.generatePrivate(x509KeySpec);

            // decrypt given ciphertext
            Cipher iesCipher = Cipher.getInstance(ASYMMETRIC_CIPHER, "BC");
            iesCipher.init(Cipher.DECRYPT_MODE, privateKey, params);

            return iesCipher.doFinal(ciphertext);
        } catch (GeneralSecurityException e) {
            return null;
        }
    }
}
