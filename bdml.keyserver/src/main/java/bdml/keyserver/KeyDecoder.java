package bdml.keyserver;

import bdml.services.exceptions.MisconfigurationException;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class KeyDecoder {
    private static final String KEY_GEN_ALG = "EC";

    public static PublicKey decodePublicKey(String encodedKey) {
        byte[] encodedKeyBytes = Base64.getDecoder().decode(encodedKey);

        // ASN.1 encoded public key (sequence of AlgorithmIdentifier and key bit string)
        X509EncodedKeySpec ks = new X509EncodedKeySpec(encodedKeyBytes);

        try {
            KeyFactory kf = KeyFactory.getInstance(KEY_GEN_ALG);
            return kf.generatePublic(ks);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new MisconfigurationException(e.getMessage());
        }
    }
}
