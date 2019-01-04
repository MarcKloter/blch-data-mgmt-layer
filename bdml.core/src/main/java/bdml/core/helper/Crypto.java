package bdml.core.helper;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Arrays;

import bdml.services.exceptions.MisconfigurationException;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Crypto {
    private static MessageDigest digest;

    public static final int ENCRYPT_MODE = 1;
    public static final int DECRYPT_MODE = 2;

    // TODO: load application.properties config
    private static final String HASH_FUNCTION = "SHA-256";
    private static final String SYMMETRIC_ALGORITHM = "AES";
    // using PKCS#7 padding (identifier in the SUN provider misleading)
    private static final String SYMMETRIC_CIPHER = "AES/CBC/PKCS5Padding";

    static {
        try {
            digest = MessageDigest.getInstance(HASH_FUNCTION);
        } catch (NoSuchAlgorithmException e) {
            throw new MisconfigurationException(e.getMessage());
        }
    }

    public static byte[] symmetricallyEncrypt(byte[] capability, byte[] plaintext) {
        SecretKeySpec key = new SecretKeySpec(capability, SYMMETRIC_ALGORITHM);

        // generate initialization vector
        byte[] ivBytes = new byte[16];
        new SecureRandom().nextBytes(ivBytes);
        IvParameterSpec iv = new IvParameterSpec(ivBytes);

        try {
            Cipher cipher = Cipher.getInstance(SYMMETRIC_CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            byte[] ciphertext = cipher.doFinal(plaintext);

            // append iv to ciphertext
            return concatenate(ciphertext, ivBytes);
        } catch (GeneralSecurityException e) {
            // TODO: handle exception for wrong key seperately (return null)
            throw new MisconfigurationException(e.getMessage());
        }
    }

    public static byte[] symmetricallyDecrypt(byte[] capability, byte[] ciphertext) {
        SecretKeySpec key = new SecretKeySpec(capability, SYMMETRIC_ALGORITHM);

        // read initialization vector from ciphertext
        int index = ciphertext.length - 16;
        byte[] ivBytes = Arrays.copyOfRange(ciphertext, index, ciphertext.length);
        ciphertext = Arrays.copyOf(ciphertext, index);
        IvParameterSpec iv = new IvParameterSpec(ivBytes);

        try {
            Cipher cipher = Cipher.getInstance(SYMMETRIC_CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            return cipher.doFinal(ciphertext);
        } catch (GeneralSecurityException e) {
            // TODO: handle exception for wrong key seperately (return null)
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
        return digest.digest(message);
    }

    /**
     * Applies the message digest algorithm set in the application configuration on the given input.
     *
     * @param message UTF-8 string representation of the message
     * @return A byte array containing the message digest.
     */
    public static byte[] hashValue(String message) {
        return digest.digest(message.getBytes(StandardCharsets.UTF_8));
    }


    /**
     * Concatenate two byte arrays.
     */
    public static byte[] concatenate(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }
}
