package bdml.services.api.types;

import java.util.Set;
import java.util.function.Function;

public interface Data {
    ParsedPayload resolveAttachments(Function<String, byte[]> converter);
}
