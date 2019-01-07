package bdml.services.api.types;

import java.util.List;

public class Data {
    private final String data;
    private final List<String> attachments;

    public Data(String data, List<String> attachments) {
        this.data = data;
        this.attachments = attachments;
    }

    public String getData() {
        return data;
    }

    public List<String> getAttachments() {
        return attachments;
    }
}
