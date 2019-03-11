package bdml.core.domain;

import bdml.core.persistence.Frame;
import bdml.core.persistence.Payload;


public class ValidDocument {
    private final Capability capability;
    private final Payload payload;

    public ValidDocument(Payload payload, Capability capability) {
        this.capability = capability;
        this.payload = payload;
    }


    public Capability getCapability() {
        return capability;
    }

    public DataIdentifier getIdentifier() {
        return capability.getIdentifier();
    }

    public Payload getPayload() {
        return payload;
    }

}
