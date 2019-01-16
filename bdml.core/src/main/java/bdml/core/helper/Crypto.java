package bdml.core.helper;

import java.security.*;
import java.util.Arrays;

import bdml.core.domain.Capability;
import bdml.services.exceptions.MisconfigurationException;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Crypto {
    // TODO: load default.application.properties config
    private static final String HASH_FUNCTION = "SHA-256";
    private static final String SYMMETRIC_ALGORITHM = "AES";
    private static final String SYMMETRIC_CIPHER = "AES/GCM/NoPadding";
    private static final int AUTHENTICATION_TAG_SIZE = 128;
    private static final int IV_BYTES = 12;

    /**
     *
     * @param capability
     * @param plaintext
     * @return
     */
    public static byte[] symmetricallyEncrypt(Capability capability, byte[] plaintext) {
        SecretKeySpec key = new SecretKeySpec(capability.toByteArray(), SYMMETRIC_ALGORITHM);

        // generate initialization vector
        byte[] ivBytes = new byte[IV_BYTES];
        new SecureRandom().nextBytes(ivBytes);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(AUTHENTICATION_TAG_SIZE, ivBytes);

        try {
            Cipher cipher = Cipher.getInstance(SYMMETRIC_CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);
            byte[] ciphertext = cipher.doFinal(plaintext);

            // append iv to ciphertext
            return concatenate(ciphertext, ivBytes);
        } catch (GeneralSecurityException e) {
            throw new MisconfigurationException(e.getMessage());
        }
    }

    /**
     *
     * @param capability
     * @param ciphertext
     * @return
     */
    public static byte[] symmetricallyDecrypt(Capability capability, byte[] ciphertext) {
        SecretKeySpec key = new SecretKeySpec(capability.toByteArray(), SYMMETRIC_ALGORITHM);

        // read initialization vector from ciphertext
        int index = ciphertext.length - IV_BYTES;
        byte[] ivBytes = Arrays.copyOfRange(ciphertext, index, ciphertext.length);
        ciphertext = Arrays.copyOf(ciphertext, index);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(AUTHENTICATION_TAG_SIZE, ivBytes);

        try {
            Cipher cipher = Cipher.getInstance(SYMMETRIC_CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);
            return cipher.doFinal(ciphertext);
        } catch(InvalidKeyException e) {
            throw new IllegalArgumentException(e.getMessage());
        } catch (GeneralSecurityException e) {
            throw new MisconfigurationException(e.getMessage());
        }
    }

    /**
     * Applies the message digest algorithm set in the application configuration on the given input.
     *
     * @param message byte array representation of the message
     * @return A byte array containing the message digest.
     */
    public static byte[] hashValue(byte[] message) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_FUNCTION);
            return digest.digest(message);
        } catch (NoSuchAlgorithmException e) {
            throw new MisconfigurationException(e.getMessage());
        }
    }

    /**
     * Concatenate two byte arrays.
     *
     * @param a first byte array to concatenate
     * @param b second byte array to concatenate
     * @return byte array of length {@code a.length + b.length} containing the content of {@code a} and {@code b}.
     */
    public static byte[] concatenate(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }
}
