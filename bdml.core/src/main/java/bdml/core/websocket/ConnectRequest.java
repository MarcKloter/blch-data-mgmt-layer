package bdml.core.websocket;

import bdml.core.domain.Account;
import bdml.core.domain.Subject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ConnectRequest {
    private Account account;

    @JsonCreator
    public ConnectRequest(@JsonProperty("identifier") String identifier,
                          @JsonProperty("password") String password) {
        this.account = new Account(Subject.decode(identifier), password);
    }

    public Account getAccount() {
        return account;
    }
}
