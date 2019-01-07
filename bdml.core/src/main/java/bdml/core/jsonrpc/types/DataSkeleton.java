package bdml.core.jsonrpc.types;

import bdml.services.api.types.Data;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class DataSkeleton {
    private String data;
    private List<String> attachments;

    public DataSkeleton(Data data) {
        this.data = data.getData();
        this.attachments = data.getAttachments();
    }

    @JsonGetter("data")
    public String getData() {
        return data;
    }

    @JsonGetter("attachments")
    public List<String> getAttachments() {
        return attachments;
    }
}
