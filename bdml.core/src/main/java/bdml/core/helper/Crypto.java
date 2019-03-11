package bdml.core.helper;

import java.security.*;
import java.util.Arrays;

import bdml.core.domain.Capability;
import bdml.services.exceptions.MisconfigurationException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Crypto {
    private Crypto() {
        throw new IllegalStateException("Utility class");
    }

    private static final String HASH_FUNCTION = "SHA-256";
    private static final String SYMMETRIC_ALGORITHM = "AES";
    // using PKCS#7 padding (identifier in the SUN provider misleading)
    private static final String SYMMETRIC_CIPHER = "AES/CBC/PKCS5Padding";
    private static final int IV_BYTES = 16;

    /**
     * Symmetrically encrypts the given plaintext using the provided {@code capability} as key.
     * The cipher used is defined in {@link Crypto#SYMMETRIC_CIPHER}.
     *
     * @param capability {@link Capability#toByteArray()} bytes to use as key
     * @param plaintext byte array containing the plaintext
     * @return Byte array containing the ciphertext || initialization vector.
     * @throws MisconfigurationException if there is an error with the configuration (eg. missing security provider)
     */
    public static byte[] symmetricallyEncrypt(Capability capability, byte[] plaintext) {
        SecretKeySpec key = new SecretKeySpec(capability.toByteArray(), SYMMETRIC_ALGORITHM);

        // generate initialization vector
        byte[] ivBytes = new byte[IV_BYTES];
        new SecureRandom().nextBytes(ivBytes);
        IvParameterSpec iv = new IvParameterSpec(ivBytes);

        try {
            Cipher cipher = Cipher.getInstance(SYMMETRIC_CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            byte[] ciphertext = cipher.doFinal(plaintext);

            // append iv to ciphertext
            return concatenate(ciphertext, ivBytes);
        } catch (GeneralSecurityException e) {
            throw new MisconfigurationException(e.getMessage());
        }
    }

    /**
     * Symmetrically decrypts the given ciphertext using the provided {@code capability} as key.
     * The cipher used is defined in {@link Crypto#SYMMETRIC_CIPHER}.
     *
     * @param capability {@link Capability#toByteArray()} bytes to use as key
     * @param ciphertext byte array containing the ciphertex
     * @return Byte array containing the plaintext || initialization vector.
     * @throws IllegalArgumentException if the given {@code ciphertext} could not be decrypted using the provided {@code capability}.
     * @throws MisconfigurationException if there is an error with the configuration (eg. missing security provider)
     */
    public static byte[] symmetricallyDecrypt(Capability capability, byte[] ciphertext) {
        SecretKeySpec key = new SecretKeySpec(capability.toByteArray(), SYMMETRIC_ALGORITHM);

        // separate initialization vector from ciphertext
        int index = ciphertext.length - IV_BYTES;
        byte[] ivBytes = Arrays.copyOfRange(ciphertext, index, ciphertext.length);
        IvParameterSpec iv = new IvParameterSpec(ivBytes);
        ciphertext = Arrays.copyOf(ciphertext, index);

        try {
            Cipher cipher = Cipher.getInstance(SYMMETRIC_CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            return cipher.doFinal(ciphertext);
        } catch(InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
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
