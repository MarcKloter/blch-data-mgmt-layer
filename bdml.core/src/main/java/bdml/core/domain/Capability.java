package bdml.core.domain;

import bdml.core.helper.Crypto;
import bdml.core.domain.exceptions.CapabilityFormatException;

import java.util.Arrays;

public class Capability {
    public static final int BYTES = 32;

    private final byte[] capability;
    private final DataIdentifier identifier;

    public Capability(byte[] capability) {
        if(capability == null)
            throw new CapabilityFormatException("The capability cannot be null.");

        if (capability.length != BYTES)
            throw new CapabilityFormatException(String.format("The given capability is %d bytes, expected %d bytes.", capability.length, BYTES));

        this.capability = capability;
        this.identifier = new DataIdentifier(Crypto.hashValue(capability));
    }

    public static Capability of(byte[] bytes) {
        if(bytes == null) return null;

        byte[] digest = Crypto.hashValue(bytes);
        return new Capability(digest);
    }

    public DataIdentifier getIdentifier() {
        return identifier;
    }

    public byte[] toByteArray() {
        return capability;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Capability that = (Capability) o;
        return Arrays.equals(capability, that.capability);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(capability);
    }
}
