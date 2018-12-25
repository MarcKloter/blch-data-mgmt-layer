package bdml.core.util;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.Security;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import bdml.services.exceptions.MisconfigurationException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

public class Crypto {
    private static MessageDigest digest;

    static {
        // TODO: load application.properties hash algorithm configuration
        String hashAlgorithm = "SHA-256";
        Security.addProvider(new BouncyCastleProvider());
        try {
            digest = MessageDigest.getInstance(hashAlgorithm, "BC");
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
