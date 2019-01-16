package bdml.core.jsonrpc.types;

import java.util.Set;

public class GetDataResponse {
    private final String data;
    private final Set<String> attachments;

    public GetDataResponse(String data, Set<String> attachments) {
        this.data = data;
        this.attachments = attachments;
    }

    public String getData() {
        return data;
    }

    public Set<String> getAttachments() {
        return attachments;
    }
}
