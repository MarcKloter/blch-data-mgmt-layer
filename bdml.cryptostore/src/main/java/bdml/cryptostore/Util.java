package bdml.cryptostore;

import bdml.services.exceptions.MisconfigurationException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.Security;

public class Util {
    /**
     * Calculates the SHA-256 message digest of the given String.
     *
     * @param message string to digest
     * @return SHA-256 message digest in hex representation.
     */
    public static String sha256(String message) {
        Security.addProvider(new BouncyCastleProvider());
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256", "BC");
        } catch (GeneralSecurityException e) {
            throw new MisconfigurationException(e.getMessage());
        }
        byte[] messageBytes = message.getBytes();
        byte[] messageDigest = digest.digest(messageBytes);
        return Hex.toHexString(messageDigest);
    }
}
