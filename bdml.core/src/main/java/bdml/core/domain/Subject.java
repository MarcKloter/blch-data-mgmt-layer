package bdml.core.domain;

import bdml.core.helper.Crypto;
import bdml.core.domain.exceptions.DataIdentifierFormatException;
import bdml.core.domain.exceptions.SubjectFormatException;
import bdml.services.exceptions.MisconfigurationException;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.security.PublicKey;
import java.util.Arrays;

public class Subject {
    private static final int BYTES = 20;

    private final byte[] subject;

    public Subject(byte[] subject) {
        if(subject == null)
            throw new SubjectFormatException("The subject cannot be null.");

        if (subject.length != BYTES)
            throw new SubjectFormatException(String.format("The given subject is %d bytes, expected %d bytes.", subject.length, BYTES));

        this.subject = subject;
    }

    public static Subject decode(String string) {
        byte[] bytes;
        try {
            bytes = Hex.decodeHex(string);
        } catch (DecoderException e) {
            throw new DataIdentifierFormatException(String.format("The subject '%s' is invalid: %s", string, e.getMessage()));
        }

        return new Subject(bytes);
    }

    public static Subject deriveFrom(PublicKey key) {
        // take H(key) as the entropy might not be evenly distributed within the key bytes
        byte[] keyDigest = Crypto.hashValue(key.getEncoded());

        if(keyDigest.length < BYTES)
            throw new MisconfigurationException("Crypto.hashValue(byte[]) returns less bytes than required by Subject.BYTES.");

        // take the BYTES*8 LSB as subject
        byte[] subject = Arrays.copyOfRange(keyDigest, keyDigest.length - BYTES, keyDigest.length);

        return new Subject(subject);
    }

    @Override
    public String toString() {
        return Hex.encodeHexString(subject);
    }
}
