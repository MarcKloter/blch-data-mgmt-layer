package bdml.keyserver.persistence;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Subject {
    private String identifier;
    private String publicKey;
    private Object metadata;

    @JsonCreator
    public Subject(@JsonProperty("identifier") String identifier,
                   @JsonProperty("publicKey") String publicKey,
                   @JsonProperty("metadata") String metadata) {
        this.identifier = identifier;
        this.publicKey = publicKey;
        this.metadata = metadata;
    }

    @JsonCreator
    public Subject(@JsonProperty("identifier") String identifier,
                   @JsonProperty("publicKey") String publicKey) {
        this.identifier = identifier;
        this.publicKey = publicKey;
        this.metadata = null;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public Object getMetadata() {
        return metadata;
    }
}
