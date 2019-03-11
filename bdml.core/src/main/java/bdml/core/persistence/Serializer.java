package bdml.core.persistence;

public interface Serializer {
    byte[] serializePayload(Payload payload);

    Payload deserializePayload(byte[] payload) throws DeserializationException;

    byte[] serializeDocument(Document doc);

    Document deserializeDocument(byte[] doc) throws DeserializationException;
}
