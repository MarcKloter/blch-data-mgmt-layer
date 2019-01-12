package bdml.core.helper;

import org.apache.commons.codec.binary.Hex;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ParsedPayload {
    private final String data;
    private final Map<byte[], byte[]> attachments;

    public ParsedPayload(String data) {
        this.data = data;
        this.attachments = new HashMap<>();
    }

    public String getData() {
        return data;
    }

    public void addAttachment(byte[] identifier, byte[] capability) {
        attachments.put(identifier, capability);
    }

    public Set<Map.Entry<byte[], byte[]>> getAttachments() {
        return attachments.entrySet();
    }

    public Set<String> getIdentifiersHexString() {
        return attachments.keySet().stream().map(Hex::encodeHexString).collect(Collectors.toSet());
    }
}
