package bdml.format;

import bdml.services.api.types.Data;
import bdml.services.api.types.ParsedPayload;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RawParsedPayload implements ParsedPayload {
    final String data;
    final Set<byte[]> capabilities;

    public RawParsedPayload(String data, List<byte[]> capabilities) {
        this.data = data;
        this.capabilities = Set.copyOf(capabilities);
    }

    @Override
    public Data processCapabilities(Function<byte[],String> converter) {
        return new RawData(data,capabilities.stream().map(converter).collect(Collectors.toSet()));
    }

}