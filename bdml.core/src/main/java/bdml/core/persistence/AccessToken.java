package bdml.core.persistence;

import java.util.List;

public class AccessToken {
    private int version = 0;
    private byte[] identifier = null;
    private byte[] encryptedCapability = null;

    //For Kryo
    private AccessToken(){}

    public AccessToken(int version, byte[] identifier, byte[] encryptedCapability) {
        this.version = version;
        this.encryptedCapability = encryptedCapability;
        this.identifier = identifier;

    }

    public int getVersion() {
        return version;
    }

    public byte[] getEncryptedCapability() {
        return encryptedCapability;
    }
    public byte[] getId() {
        return identifier;
    }

}
