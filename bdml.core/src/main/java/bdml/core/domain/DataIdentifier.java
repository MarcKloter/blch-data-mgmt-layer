package bdml.core.domain;

import bdml.core.domain.exceptions.DataIdentifierFormatException;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.util.Arrays;

public class DataIdentifier {
    private static final int BYTES = 32;
    private final byte[] identifier;

    public DataIdentifier(byte[] identifier) {
        if(identifier == null)
            throw new DataIdentifierFormatException("The identifier cannot be null.");

        if (identifier.length != BYTES)
            throw new DataIdentifierFormatException(String.format("The given data identifier is %d bytes, expected %d bytes.", identifier.length, BYTES));

        this.identifier = identifier;
    }

    public static DataIdentifier decode(String string) {
        byte[] bytes;
        try {
            bytes = Hex.decodeHex(string);
        } catch (DecoderException e) {
            throw new DataIdentifierFormatException(String.format("The identifier '%s' is invalid: %s", string, e.getMessage()));
        }

        return new DataIdentifier(bytes);
    }

    public byte[] toByteArray() {
        return identifier;
    }

    @Override
    public String toString() {
        return Hex.encodeHexString(identifier);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(identifier);
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) return true;
        if (!(other instanceof DataIdentifier)) return false;
        final DataIdentifier that = (DataIdentifier) other;

        return Arrays.equals(this.toByteArray(), that.toByteArray());
    }
}
