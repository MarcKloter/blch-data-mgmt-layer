package bdml.core.persistence;

import bdml.core.domain.Capability;

import java.util.Set;

public class Payload {
    private final String data;
    private final Set<Capability> attachments;
    private final byte[] nonce;

    public Payload(String data, Set<Capability> attachments, byte[] nonce) {
        this.data = data;
        this.attachments = attachments;
        this.nonce = nonce;
    }

    public byte[] getNonce() {
        return nonce;
    }

    public String getData() {
        return data;
    }

    public Set<Capability> getAttachments() {
        return attachments;
    }
}
