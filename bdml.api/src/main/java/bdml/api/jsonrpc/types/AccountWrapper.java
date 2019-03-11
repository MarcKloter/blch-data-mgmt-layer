package bdml.api.jsonrpc.types;

import bdml.core.domain.Account;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AccountWrapper {
    private Account account;

    @JsonCreator
    public AccountWrapper(@JsonProperty("identifier") String identifier,
                          @JsonProperty("password") String password) {
        this.account = new Account(Subject.decodeAddress(identifier), password);
    }

    public Account unwrap() {
        return account;
    }
}
