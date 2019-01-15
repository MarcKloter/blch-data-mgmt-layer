package bdml.format;
import bdml.format.proto.Raw;
import bdml.services.Formater;
import bdml.services.api.types.ParsedPayload;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class FormaterImpl implements Formater {

    static List<ByteString> encode(Collection<byte[]> c) {
        return c.stream().map(ByteString::copyFrom).collect(Collectors.toList());
    }

    public FormaterImpl(Properties configuration) {

    }

    @Override
    public byte[] serialize(ParsedPayload data) {
        //Hardcoded for now
        RawParsedPayload raw = (RawParsedPayload)data;
        return Raw.RawData.newBuilder()
                .setData(raw.data)
                .addAllAttachedCapability(FormaterImpl.encode(raw.capabilities))
                .build()
                .toByteArray();
    }

    @Override
    public ParsedPayload parse(byte[] payload) {
        //Hardcoded for now
        try {
            Raw.RawData data = Raw.RawData.parseFrom(payload);
            List<byte[]> capabilities = data.getAttachedCapabilityList().stream().map(ByteString::toByteArray).collect(Collectors.toList());
            return new RawParsedPayload(data.getData(),capabilities);
        } catch (InvalidProtocolBufferException e) {
            throw new IllegalStateException("The format of the payload is invalid.");
        }
    }
}