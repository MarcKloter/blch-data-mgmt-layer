package integration;

import bdml.core.Core;
import bdml.core.CoreService;
import bdml.core.domain.Subject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CreateAccountIT {
    private static final String PASSWORD = "password";

    private Core core;

    @BeforeAll
    void setup() {
        this.core = CoreService.getInstance();
    }

    @Test
    void Create_Account() {
        Subject subject = core.createAccount(PASSWORD);
        assertNotNull(subject);
    }

    @Test
    void Empty_Password() {
        assertThrows(IllegalArgumentException.class, () -> core.createAccount(""));
    }

    @Test
    void Null_Password() {
        assertThrows(NullPointerException.class, () -> core.createAccount(null));
    }
}
