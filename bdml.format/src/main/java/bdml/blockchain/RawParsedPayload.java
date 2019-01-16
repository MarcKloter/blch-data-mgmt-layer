package bdml.blockchain;

import bdml.services.api.types.Data;
import bdml.services.api.types.ParsedPayload;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RawParsedPayload implements ParsedPayload {
    String data;
    Set<byte[]> capabilities;

    //Needed by kryo
    private RawParsedPayload(){}

    public RawParsedPayload(String data, List<byte[]> capabilities) {
        this.data = data;
        this.capabilities = new HashSet<>(capabilities);
    }

    @Override
    public Data processCapabilities(Function<byte[],String> converter) {
        return new RawData(data,capabilities.stream().map(converter).collect(Collectors.toSet()));
    }

}