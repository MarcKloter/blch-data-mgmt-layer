package bdml.api.jsonrpc.raw;

import bdml.core.domain.Capability;
import bdml.core.domain.Data;
import bdml.core.domain.DataIdentifier;
import bdml.core.persistence.Payload;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RawPayload implements Payload {
    private String data = null;
    private Set<Capability> attachments = null;

    private RawPayload() {}

    RawPayload(String data, Set<Capability> attachments) {
        this.data = data;
        this.attachments = attachments;
    }


    @Override
    public Data processCapabilities(Function<Capability, DataIdentifier> converter) {
        return new RawData(data, attachments.stream().map(converter).collect(Collectors.toSet()));
    }

    @Override
    public boolean isValid() {
        return !attachments.contains(null);
    }
}
