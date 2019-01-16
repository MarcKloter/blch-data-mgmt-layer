package integration;

import bdml.core.CoreService;
import bdml.core.Core;
import bdml.core.domain.DataIdentifier;
import bdml.core.domain.exceptions.AuthenticationException;
import bdml.core.domain.Account;
import bdml.core.domain.Data;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import spark.utils.Assert;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BasicIT {
    //TODO: working directory currently is bdml.core

    private static final String PASSWORD_1 = "password1";
    private static final String PASSWORD_2 = "password2";
    private static final String PASSWORD_3 = "password3";

    private static final Data DATA_1 = new Data("data string 1", null);
    private static final Data DATA_2 = new Data("data string 2", null);

    private Core core;

    private Account account1;
    private Account account2;
    private Account account3;

    @BeforeAll
    public void setup() {
        this.core = CoreService.getInstance();

        this.account1 = new Account(core.createAccount(PASSWORD_1), PASSWORD_1);
        this.account2 = new Account(core.createAccount(PASSWORD_2), PASSWORD_2);
        this.account3 = new Account(core.createAccount(PASSWORD_3), PASSWORD_3);
    }

    @Test
    public void Store_And_Retrieve_Data() throws AuthenticationException {
        Assert.notNull(account1);

        DataIdentifier identifier = core.storeData(DATA_1, account1);

        Data result = core.getData(identifier, account1);
        Assert.notNull(result);
        Assert.isTrue(result.getData().equals(DATA_1.getData()), "the data returned does not match the data stored");
    }
}
