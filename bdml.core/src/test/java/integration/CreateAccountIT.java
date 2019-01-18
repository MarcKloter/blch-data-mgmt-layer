package integration;

import bdml.core.Core;
import bdml.core.CoreService;
import bdml.core.domain.Subject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CreateAccountIT {
    //TODO: working directory currently is bdml.core

    private static final String PASSWORD = "password";

    private Core core;

    @BeforeAll
    public void setup() {
        this.core = CoreService.getInstance();
    }

    @Test
    public void Create_Account() {
        Subject subject = core.createAccount(PASSWORD);
        assertNotNull(subject);
    }

    @Test
    public void Empty_Password() {
        assertThrows(IllegalArgumentException.class, () -> core.createAccount(""));
    }

    @Test
    public void Null_Password() {
        assertThrows(NullPointerException.class, () -> core.createAccount(null));
    }
}
