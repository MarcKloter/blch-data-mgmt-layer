package bdml.core.persistence;

import bdml.core.domain.Capability;
import bdml.core.domain.Data;
import bdml.core.domain.DataIdentifier;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public interface Payload {
    Data processCapabilities(Function<Capability, DataIdentifier> converter);
    boolean isValid();
}
