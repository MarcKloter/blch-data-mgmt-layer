package bdml.core.jsonrpc.types;

import bdml.services.api.types.Account;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AccountWrapper {
    private Account account;

    @JsonCreator
    public AccountWrapper(@JsonProperty("address") String address,
                          @JsonProperty("password") String password) {
        this.account = new Account(address, password);
    }

    public Account unwrap() {
        return account;
    }
}
