package bdml.core.domain;

import java.util.Set;

public class Data {
    private final String data;
    private final Set<DataIdentifier> attachments;

    public Data(String data, Set<DataIdentifier> attachments) {
        this.data = data;
        this.attachments = attachments;
    }

    public String getData() {
        return data;
    }

    public Set<DataIdentifier> getAttachments() {
        return attachments;
    }
}
