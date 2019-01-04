package bdml.core.helper;

import bdml.core.proto.FrameOuterClass;

/**
 * Adds meta information to a proto buffer Frame message.
 */
public class FrameWrapper {
    private byte[] identifier;
    private byte[] capability;
    private FrameOuterClass.Frame frame;

    public FrameWrapper(byte[] identifier, byte[] capability, FrameOuterClass.Frame frame) {
        this.identifier = identifier;
        this.capability = capability;
        this.frame = frame;
    }

    public byte[] getIdentifier() {
        return identifier;
    }

    public byte[] getCapability() {
        return capability;
    }

    public byte[] getBytes() {
        return frame.toByteArray();
    }

    public FrameOuterClass.Frame unwrap() {
        return frame;
    }
}
