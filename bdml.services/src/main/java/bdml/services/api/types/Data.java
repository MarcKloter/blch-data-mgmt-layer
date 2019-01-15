package bdml.services.api.types;

import java.util.Set;
import java.util.function.Function;

public interface Data {
    Set<String> getAttachments();
    ParsedPayload resolveAttachments(Function<String, byte[]> converter);
}
