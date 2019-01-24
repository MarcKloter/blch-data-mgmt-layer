package bdml.api.jsonrpc.types;

import bdml.api.jsonrpc.raw.RawData;
import bdml.core.domain.Data;
import bdml.core.domain.DataIdentifier;

import java.util.Set;
import java.util.stream.Collectors;

public class GetDataResponse {
    private final String data;
    private final Set<String> attachments;

    private GetDataResponse(String data, Set<String> attachments) {
        this.data = data;
        this.attachments = attachments;
    }

    public static GetDataResponse of(Data data) {
        if(data == null) return null;

        Set<String> attachments = ((RawData)data).getAttachments().stream().map(DataIdentifier::toString).collect(Collectors.toSet());
        return new GetDataResponse(((RawData)data).getData(), attachments);
    }

    public String getData() {
        return data;
    }

    public Set<String> getAttachments() {
        return attachments;
    }
}
