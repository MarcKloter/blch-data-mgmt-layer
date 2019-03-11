package bdml.core.persistence;

public class Document {
    private int version = 0;
    private byte[] payload = null;

    //For Kryo
    private Document(){}

    public Document(int version, byte[] payload) {
        this.version = version;
        this.payload = payload;
    }

    public int getVersion() {
        return version;
    }

    public byte[] getPayload() {
        return payload;
    }
}
