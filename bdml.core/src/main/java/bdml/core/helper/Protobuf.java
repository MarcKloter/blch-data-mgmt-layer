package bdml.core.helper;

import bdml.core.proto.FrameOuterClass;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Protobuf {
    // TODO: javadoc

    /**
     * Builds FrameOuterClass.Payload and returns its byte array representation.
     *
     * @param data
     * @param attachedCapabilities
     * @return
     */
    public static byte[] buildPayload(String data, Collection<byte[]> attachedCapabilities) {
        return FrameOuterClass.Payload.newBuilder()
                .setData(data)
                .addAllAttachedCapability(encode(attachedCapabilities))
                .build()
                .toByteArray();
    }

    public static FrameOuterClass.Payload parsePayload(byte[] payload) {
        try {
            return FrameOuterClass.Payload.parseFrom(payload);
        } catch (InvalidProtocolBufferException e) {
            throw new IllegalStateException("The format of the payload is invalid.");
        }
    }

    public static FrameOuterClass.Frame buildFrame(int version, Collection<byte[]> encCap, byte[] encPayload) {
        return FrameOuterClass.Frame.newBuilder()
                .setVersion(version)
                .addAllEncryptedCapability(encode(encCap))
                .setEncryptedPayload(encode(encPayload))
                .build();
    }

    public static FrameOuterClass.Frame parseFrame(byte[] frame, int version) {
        if(frame == null)
            return null;

        try {
            FrameOuterClass.Frame profbufFrame = FrameOuterClass.Frame.parseFrom(frame);

            if(profbufFrame.getVersion() != version)
                throw new IllegalStateException("The version of the frame is invalid.");

            return profbufFrame;
        } catch (InvalidProtocolBufferException e) {
            throw new IllegalStateException("The format of the frame is invalid.");
        }
    }

    public static List<ByteString> encode(Collection<byte[]> c) {
        return c.stream().map(ByteString::copyFrom).collect(Collectors.toList());
    }

    public static ByteString encode(byte[] bytes) {
        return ByteString.copyFrom(bytes);
    }

    public static List<byte[]> decode(Collection<ByteString> c) {
        return c.stream().map(ByteString::toByteArray).collect(Collectors.toList());
    }

    public static byte[] decode(ByteString c) {
        return c.toByteArray();
    }
}
