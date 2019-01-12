package bdml.services.api.types;

import java.util.Set;

public class Data {
    private final String data;
    private final Set<String> attachments;

    public Data(String data, Set<String> attachments) {
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
