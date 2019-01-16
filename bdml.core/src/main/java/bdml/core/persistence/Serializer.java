package bdml.core.persistence;

public interface Serializer {
    byte[] serializePayload(Payload payload);

    Payload deserializePayload(byte[] payload) throws DeserializationException;

    byte[] serializeFrame(Frame frame);

    Frame deserializeFrame(byte[] frame) throws DeserializationException;
}
