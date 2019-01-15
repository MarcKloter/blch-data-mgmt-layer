package bdml.services;

import bdml.services.api.types.ParsedPayload;

public interface Formater {
    // TODO: javadoc

    byte[] serialize(ParsedPayload data);
    ParsedPayload parse(byte[] data);
}
