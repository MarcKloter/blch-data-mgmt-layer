package bdml.core.helper;

import bdml.core.RawData;
import bdml.services.api.types.Data;
import bdml.services.api.types.ParsedPayload;
import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RawParsedPayload implements ParsedPayload {
    private final String data;
    private final Set<byte[]> capabilities;

    public RawParsedPayload(String data, List<byte[]> capabilities) {
        this.data = data;
        this.capabilities = ImmutableSet.copyOf(capabilities.iterator());
    }

    @Override
    public Data processCapabilities(Function<byte[],String> converter) {
        return new RawData(data,capabilities.stream().map(converter).collect(Collectors.toSet()));
    }

    @Override
    public byte[] serialize() {
        return Protobuf.buildRawData(data,capabilities);
    }
}
