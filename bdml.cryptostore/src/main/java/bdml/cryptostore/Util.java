package bdml.cryptostore;

import bdml.services.exceptions.MisconfigurationException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;

public class Util {
    /**
     * Calculates the SHA-256 message digest of the given String.
     *
     * @param message string to digest
     * @return SHA-256 message digest in base64 representation.
     */
    public static String sha256(String message) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (GeneralSecurityException e) {
            throw new MisconfigurationException(e.getMessage());
        }
        byte[] messageBytes = message.getBytes();
        byte[] messageDigest = digest.digest(messageBytes);
        return Base64.getEncoder().encodeToString(messageDigest);
    }

    /**
     * Concatenate two byte arrays.
     * https://stackoverflow.com/a/5513188/4382892
     */
    public static byte[] concat(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }
}
