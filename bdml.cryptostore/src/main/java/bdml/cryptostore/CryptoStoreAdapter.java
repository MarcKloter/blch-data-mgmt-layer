package bdml.cryptostore;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;

import bdml.cryptostore.persistence.KeyPairList;
import bdml.services.CryptographicStore;
import bdml.services.exceptions.MisconfigurationException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.IESParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class CryptoStoreAdapter implements CryptographicStore {
    // TODO: load KEY_GEN_ALG and EC_CURVE from configuration file
    private final String KEY_GEN_ALG = "EC";
    private final String ASYMMETRIC_CIPHER = "ECIESwithAES-CBC";
    private final String EC_CURVE = "secp256k1";
    //private final String EC_CURVE = "secp521r1";
    //private final String EC_CURVE = "secp384r1";

    private KeyPairList keyPairs;

    public CryptoStoreAdapter() {
        this.keyPairs = new KeyPairList();
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

        // get private key persisted for the given public key and secret combination
        byte[] decodedKey = keyPairs.get(publicKey, secret);

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
            switch (e.getCause().getClass().getSimpleName()) {
                case "InvalidCipherTextException":
                    return null;
                default:
                    throw new MisconfigurationException(e.getMessage());
            }
        }
    }
}
