package bdml.core.websocket;

import bdml.services.api.types.Account;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ConnectRequest {
    private Account account;

    @JsonCreator
    public ConnectRequest(@JsonProperty("identifier") String identifier,
                          @JsonProperty("password") String password) {
        this.account = new Account(identifier, password);
    }

    public Account getAccount() {
        return account;
    }
}
