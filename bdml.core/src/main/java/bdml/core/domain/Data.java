package bdml.core.domain;

import bdml.core.persistence.Payload;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public interface Data {
    Payload resolveAttachments(Function<DataIdentifier, Capability> converter);
}
