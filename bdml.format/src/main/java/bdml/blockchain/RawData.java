package bdml.blockchain;

import bdml.services.api.types.Data;
import bdml.services.api.types.ParsedPayload;

import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RawData implements Data {
    private final String data;
    private final Set<String> attachements;

    public RawData(String data, Set<String> attachements) {
        this.data = data;
        if(attachements == null) {
            this.attachements = Collections.EMPTY_SET;
        } else {
            this.attachements = Collections.unmodifiableSet(attachements);
        }
    }

    public String getData() {
        return data;
    }

    @Override
    public ParsedPayload resolveAttachments(Function<String, byte[]> converter) {
        return new RawParsedPayload(data,attachements.stream().map(converter).collect(Collectors.toList()));
    }
}
