package bdml.core;

import bdml.core.helper.RawParsedPayload;
import bdml.services.api.types.Data;
import bdml.services.api.types.ParsedPayload;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RawData implements Data {
    private final String data;
    private final Set<String> attachements;

    public RawData(String data, Set<String> attachements) {
        this.data = data;
        this.attachements = attachements;
    }

    public String getData() {
        return data;
    }

    @Override
    public Set<String> getAttachments() {
        return attachements;
    }

    @Override
    public ParsedPayload resolveAttachments(Function<String, byte[]> converter) {
        return new RawParsedPayload(data,attachements.stream().map(converter).collect(Collectors.toList()));
    }
}
