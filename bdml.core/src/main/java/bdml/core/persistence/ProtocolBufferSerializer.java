package bdml.core.persistence;

import bdml.core.persistence.protobuf.FrameOuterClass;
import bdml.core.domain.Capability;
import bdml.core.domain.exceptions.CapabilityFormatException;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ProtocolBufferSerializer implements Serializer {

    @Override
    public byte[] serializePayload(Payload payload) {
        Set<ByteString> attachments = payload.getAttachments().stream()
                .map(Capability::toByteArray)
                .map(ByteString::copyFrom)
                .collect(Collectors.toSet());
        return FrameOuterClass.Payload.newBuilder()
                .setData(payload.getData())
                .addAllAttachedCapability(attachments)
                .setNonce(encodeByteString(payload.getNonce()))
                .build()
                .toByteArray();
    }

    @Override
    public Payload deserializePayload(byte[] payload) throws DeserializationException {
        FrameOuterClass.Payload pbPayload;
        try {
            pbPayload = FrameOuterClass.Payload.parseFrom(payload);
        } catch (InvalidProtocolBufferException e) {
            throw new DeserializationException("The format of the payload is invalid.");
        }

        String data = pbPayload.getData();
        byte[] nonce = decodeByteString(pbPayload.getNonce());

        Set<Capability> attachments;
        try {
            attachments = pbPayload.getAttachedCapabilityList().stream()
                    .map(ByteString::toByteArray)
                    .map(Capability::new)
                    .collect(Collectors.toSet());
        } catch (CapabilityFormatException e) {
            throw new DeserializationException(String.format("The format of the frame is invalid: %s", e.getMessage()));
        }

        return new Payload(data, attachments, nonce);
    }

    @Override
    public byte[] serializeFrame(Frame frame) {
        List<ByteString> encryptedCapability = frame.getEncryptedCapability().stream()
                .map(ByteString::copyFrom)
                .collect(Collectors.toList());
        return FrameOuterClass.Frame.newBuilder()
                .setVersion(frame.getVersion())
                .addAllEncryptedCapability(encryptedCapability)
                .setEncryptedPayload(encodeByteString(frame.getEncryptedPayload()))
                .build()
                .toByteArray();
    }

    @Override
    public Frame deserializeFrame(byte[] frame) throws DeserializationException {
        FrameOuterClass.Frame pbFrame;
        try {
            pbFrame = FrameOuterClass.Frame.parseFrom(frame);
        } catch (InvalidProtocolBufferException e) {
            throw new DeserializationException("The format of the frame is invalid.");
        }

        int version = pbFrame.getVersion();
        List<byte[]> encryptedCapabilities = pbFrame.getEncryptedCapabilityList().stream()
                .map(ByteString::toByteArray)
                .collect(Collectors.toList());
        byte[] encryptedPayload = decodeByteString(pbFrame.getEncryptedPayload());

        return new Frame(version, encryptedCapabilities, encryptedPayload);
    }

    private ByteString encodeByteString(byte[] bytes) {
        return ByteString.copyFrom(bytes);
    }

    private static byte[] decodeByteString(ByteString c) {
        return c.toByteArray();
    }
}
