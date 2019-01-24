package integration;

import bdml.core.domain.Capability;
import bdml.core.domain.Data;
import bdml.core.domain.DataIdentifier;
import bdml.core.persistence.Payload;

import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RawData implements Data {
    private String data = null;
    private Set<DataIdentifier> attachments = null;

    private RawData() {}

    public RawData(String data, Set<DataIdentifier> attachments) {
        this.data = data;
        this.attachments = Objects.requireNonNullElseGet(attachments, Set::of);
    }

    public RawData(String data) {
        this.data = data;
        this.attachments = Set.of();
    }

    @Override
    public Payload resolveAttachments(Function<DataIdentifier, Capability> converter) {
        return new RawPayload(data, attachments.stream().map(converter).collect(Collectors.toSet()));
    }

    public String getData() {
        return data;
    }

    public Set<DataIdentifier> getAttachments() {
        return attachments;
    }

}
