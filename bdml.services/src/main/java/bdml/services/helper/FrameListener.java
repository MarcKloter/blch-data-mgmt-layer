package bdml.services.helper;

public interface FrameListener {
    void update(byte[] rawIdentifier, byte[] serializedFrame);
}
