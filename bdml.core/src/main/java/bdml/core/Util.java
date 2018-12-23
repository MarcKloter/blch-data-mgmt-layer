package bdml.core;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.Security;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import bdml.services.exceptions.MisconfigurationException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

public class Util {
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

    /**
     * Wraps Objects.requireNonNull for uniform messages.
     *
     * @param obj object reference to check
     * @param param parameter name
     */
    public static void requireNonNull(Object obj, String param) {
        Objects.requireNonNull(obj, String.format("Parameter '%s' cannot be null.", param));
    }


    /**
     * Checks that the specified Collection is not empty and throws a customized IllegalArgumentException if it is.
     * In case the Collection is null, a NullPointerException is thrown.
     *
     * @param coll the Collection to check
     * @param param parameter name
     */
    public static void requireNonEmpty(Collection coll, String param) {
        requireNonNull(coll, param);
        if(coll.isEmpty())
            throw new IllegalArgumentException(String.format("No '%s' provided.", param));
    }

    /**
     * Checks that the specified String is not empty and throws a customized IllegalArgumentException if it is.
     * In case the String is null, a NullPointerException is thrown.
     *
     * @param string the String to check
     * @param param parameter name
     */
    public static void requireNonEmpty(String string, String param) {
        requireNonNull(string, param);
        if(string.isEmpty())
            throw new IllegalArgumentException(String.format("No '%s' provided.", param));
    }
}
