package bdml.services.types;

public class StorageObject {
    private byte[] identifier;
    private byte[] key;
    private byte[] object;

    public StorageObject(byte[] identifier, byte[] key, byte[] object) {
        this.identifier = identifier;
        this.key = key;
        this.object = object;
    }

    public byte[] getIdentifier() {
        return identifier;
    }

    public void setIdentifier(byte[] identifier) {
        this.identifier = identifier;
    }

    public byte[] getKey() {
        return key;
    }

    public void setKey(byte[] key) {
        this.key = key;
    }

    public byte[] getObject() {
        return object;
    }

    public void setObject(byte[] object) {
        this.object = object;
    }
}
