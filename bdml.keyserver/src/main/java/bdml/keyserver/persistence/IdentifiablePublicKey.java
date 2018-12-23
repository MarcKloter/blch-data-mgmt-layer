package bdml.keyserver.persistence;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class IdentifiablePublicKey {
    private String identifier;
    private String publicKey;

    @JsonCreator
    public IdentifiablePublicKey(@JsonProperty("identifier") String identifier,
                                 @JsonProperty("publicKey") String publicKey) {
        this.identifier = identifier;
        this.publicKey = publicKey;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getPublicKey() {
        return publicKey;
    }
}
