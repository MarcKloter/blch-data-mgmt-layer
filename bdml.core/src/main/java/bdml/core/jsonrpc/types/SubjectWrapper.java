package bdml.core.jsonrpc.types;

import bdml.services.api.types.Subject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcOptional;

public class SubjectWrapper {
    private Subject subject;

    @JsonCreator
    public SubjectWrapper(@JsonProperty("address") String address,
                          @JsonProperty("description") @JsonRpcOptional String description) {
        this.subject = new Subject(address, description);
    }

    public Subject unwrap() {
        return subject;
    }
}
