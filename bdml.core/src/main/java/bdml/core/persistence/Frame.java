package bdml.core.persistence;

import java.util.List;

public class Frame {
    private int version = 0;
    private List<byte[]> encryptedCapability = null;
    private byte[] encryptedPayload = null;

    //For Kryo
    private Frame(){}

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
