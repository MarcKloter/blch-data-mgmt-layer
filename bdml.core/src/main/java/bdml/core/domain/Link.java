package bdml.core.domain;

import java.util.Optional;

public interface Link {
    Optional<Capability> getCapability();
    DataIdentifier getIdentifier();
}
