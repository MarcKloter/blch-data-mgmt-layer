package bdml.services.api.types;

import java.util.Set;

public class Identifier {
    private final String identifier;
    private final Set<Identifier> attachments;

    public Identifier(String identifier, Set<Identifier> attachments) {
        this.identifier = identifier;
        this.attachments = attachments;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Set<Identifier> getAttachments() {
        return attachments;
    }
}
