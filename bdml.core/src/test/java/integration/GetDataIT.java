package integration;

import bdml.core.Core;
import bdml.core.CoreService;
import bdml.core.domain.Account;
import bdml.core.domain.Data;
import bdml.core.domain.DataIdentifier;
import bdml.core.domain.Subject;
import bdml.core.domain.exceptions.AuthenticationException;
import bdml.core.domain.exceptions.NotAuthorizedException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.naming.AuthenticationNotSupportedException;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GetDataIT {
    private static final String PASSWORD_1 = "password1";
    private static final String PASSWORD_2 = "password2";
    private static final String PASSWORD_3 = "password2";

    private static final Data SIMPLE_DATA_1 = new Data("data string 1");
    private static final Data SIMPLE_DATA_2 = new Data("data string 2");

    private Core core;

    private Account account1;
    private Account account2;
    private Account account3;

    private Subject subject1;
    private Subject subject2;
    private Subject subject3;

    private DataIdentifier identifier1;
    private DataIdentifier identifier2;

    @BeforeAll
    void setup() throws AuthenticationException {
        this.core = CoreService.getInstance();

        this.subject1 = core.createAccount(PASSWORD_1);
        this.account1 = new Account(subject1, PASSWORD_1);

        this.subject2 = core.createAccount(PASSWORD_2);
        this.account2 = new Account(subject2, PASSWORD_2);

        this.subject3 = core.createAccount(PASSWORD_3);
        this.account3 = new Account(subject3, PASSWORD_3);

        this.identifier1 = core.storeData(SIMPLE_DATA_1, account1);
        this.identifier2 = core.storeData(SIMPLE_DATA_2, account2);
    }

    @Test
    void Null_DataIdentifier() {
        assertThrows(NullPointerException.class, () -> core.getData(null, account1));
    }

    @Test
    void Invalid_DataIdentifier() {
        DataIdentifier invalidIdentifier = DataIdentifier.decode("0".repeat(64));
        assertThrows(IllegalArgumentException.class, () -> core.getData(invalidIdentifier, account1));
    }

    @Test
    void Null_Account() {
        assertThrows(NullPointerException.class, () -> core.getData(identifier1, null));
    }

    @Test
    void Invalid_Account() {
        Account invalidAccount = new Account(subject1, "");
        assertThrows(AuthenticationException.class, () -> core.getData(identifier1, invalidAccount));
    }

    @Test
    void Get_Data() {
        Data result1 = assertDoesNotThrow(() -> core.getData(identifier1, account1));
        assertNotNull(result1);
        assertEquals(result1.getData(), SIMPLE_DATA_1.getData());

        Data result2 = assertDoesNotThrow(() -> core.getData(identifier2, account2));
        assertNotNull(result2);
        assertEquals(result2.getData(), SIMPLE_DATA_2.getData());
    }

    @Test
    void Get_NotAuthorized_Data() {
        assertThrows(NotAuthorizedException.class, () -> core.getData(identifier2, account1));
        assertThrows(NotAuthorizedException.class, () -> core.getData(identifier1, account2));
    }

    @Test
    void Get_Addressed_Data() {
        DataIdentifier identifier = assertDoesNotThrow(() -> core.storeData(SIMPLE_DATA_1, account1, Collections.singleton(subject2)));

        // assert that the account that called storeData can read the data
        Data result1 = assertDoesNotThrow(() -> core.getData(identifier, account1));
        assertNotNull(result1);
        assertEquals(result1.getData(), SIMPLE_DATA_1.getData());

        // assert that the account that was given as subject can read the data
        Data result2 = assertDoesNotThrow(() -> core.getData(identifier, account2));
        assertNotNull(result2);
        assertEquals(result2.getData(), SIMPLE_DATA_1.getData());

        // assert that an unrelated account cannot read the data
        assertThrows(NotAuthorizedException.class, () -> core.getData(identifier, account3));
    }

    @Test
    void Get_Data_With_Attachment() {
        Set<DataIdentifier> attachment = Set.of(identifier1);
        Data dataWithAttachment = new Data(SIMPLE_DATA_1.getData(), attachment);
        DataIdentifier identifier = assertDoesNotThrow(() -> core.storeData(dataWithAttachment, account1));

        Data result = assertDoesNotThrow(() -> core.getData(identifier, account1));
        assertNotNull(result);
        assertEquals(result.getData(), SIMPLE_DATA_1.getData());
        assertEquals(result.getAttachments().size(), attachment.size());
        assertTrue(result.getAttachments().contains(identifier1));
    }
}
