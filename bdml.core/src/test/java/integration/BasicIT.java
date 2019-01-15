package integration;

import bdml.core.CoreService;
import bdml.format.RawData;
import bdml.services.api.Core;
import bdml.services.api.exceptions.AuthenticationException;
import bdml.services.api.types.Account;
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

    private static final RawData DATA_1 = new RawData("data string 1", null);
    private static final RawData DATA_2 = new RawData("data string 2", null);

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

        String identifier = core.storeData(DATA_1, account1);
        Assert.isTrue(identifier.length() == 64, "returned identifier format invalid");

        RawData result = (RawData)core.getData(identifier, account1);
        Assert.notNull(result);
        Assert.isTrue(result.getData().equals(DATA_1.getData()), "the data returned does not match the data stored");
    }
}
