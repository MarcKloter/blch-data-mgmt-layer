package bdml.services.api.types;

import java.util.List;

public class Data {
    private String data;
    private List<String> attachments;

    public Data(String data, List<String> attachments) {
        this.data = data;
        this.attachments = attachments;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public List<String> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<String> attachments) {
        this.attachments = attachments;
    }
}
