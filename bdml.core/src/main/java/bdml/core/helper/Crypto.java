package bdml.core.helper;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.Security;

import bdml.services.exceptions.MisconfigurationException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Crypto {
    private static MessageDigest digest;

    // TODO: load application.properties config
    private static final String HASH_FUNCTION = "SHA-256";
    private static final String SYMMETRIC_ALGORITHM = "AES";
    private static final String SYMMETRIC_CIPHER = "AES/CBC/PKCS7Padding";

    static {
        Security.addProvider(new BouncyCastleProvider());
        try {
            digest = MessageDigest.getInstance(HASH_FUNCTION, "BC");
        } catch (GeneralSecurityException e) {
            throw new MisconfigurationException(e.getMessage());
        }
    }

    public static byte[] symmetricallyEncrypt(byte[] capability, byte[] plaintext) {
        SecretKeySpec key = new SecretKeySpec(capability, SYMMETRIC_ALGORITHM);
        try {
            Cipher cipher = Cipher.getInstance(SYMMETRIC_CIPHER, "BC");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(plaintext);
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
        return digest.digest(message);
    }

    /**
     * Applies the message digest algorithm set in the application configuration on the given input.
     *
     * @param message hex string representation of the message
     * @return A string containing the hex representation of the message digest.
     */
    public static String hashValue(String message) {
        return Hex.toHexString(digest.digest(Hex.decode(message)));
    }

}
