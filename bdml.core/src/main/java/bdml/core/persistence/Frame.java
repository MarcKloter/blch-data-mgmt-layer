package bdml.core.persistence;

import java.util.List;

public class Frame {
    private final int version;
    private final List<byte[]> encryptedCapability;
    private final byte[] encryptedPayload;

    public Frame(int version, List<byte[]> encryptedCapability, byte[] encryptedPayload) {
        this.version = version;
        this.encryptedCapability = encryptedCapability;
        this.encryptedPayload = encryptedPayload;
    }

    public int getVersion() {
        return version;
    }

    public List<byte[]> getEncryptedCapability() {
        return encryptedCapability;
    }

    public byte[] getEncryptedPayload() {
        return encryptedPayload;
    }
}
