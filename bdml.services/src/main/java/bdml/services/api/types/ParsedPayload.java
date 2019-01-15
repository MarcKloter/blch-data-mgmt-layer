package bdml.services.api.types;

import java.util.function.Function;

public interface ParsedPayload {
    Data processCapabilities(Function<byte[],String> converter);
    byte[] serialize();
}
