package bdml.core.domain;

import bdml.core.persistence.Frame;

/**
 * Extends the structure of a parsed {@link Frame}, where {@link Frame#getEncryptedCapability()} has been decrypted.
 */
public class ParsedFrame extends Frame {
    private final Capability capability;

    public ParsedFrame(Frame frame, Capability capability) {
        super(frame.getVersion(), frame.getEncryptedCapability(), frame.getEncryptedPayload());
        this.capability = capability;
    }

    public Capability getCapability() {
        return capability;
    }

    public DataIdentifier getIdentifier() {
        return capability.getIdentifier();
    }
}
