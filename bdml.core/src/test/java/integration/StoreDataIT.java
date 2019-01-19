package integration;

import bdml.core.CoreService;
import bdml.core.Core;
import bdml.core.domain.DataIdentifier;
import bdml.core.domain.Subject;
import bdml.core.domain.exceptions.AuthenticationException;
import bdml.core.domain.Account;
import bdml.core.domain.Data;
import bdml.core.domain.exceptions.NotAuthorizedException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StoreDataIT {
    //TODO: working directory currently is bdml.core

    private static final String PASSWORD_1 = "password1";
    private static final String PASSWORD_2 = "password2";

    private static final Data DATA = new Data("data string");

    private Core core;

    private Account account1;
    private Account account2;

    private Subject subject1;
    private Subject subject2;

    @BeforeAll
    void setup() {
        this.core = CoreService.getInstance();

        this.subject1 = core.createAccount(PASSWORD_1);
        this.account1 = new Account(subject1, PASSWORD_1);

        this.subject2 = core.createAccount(PASSWORD_2);
        this.account2 = new Account(subject2, PASSWORD_2);
    }

    @Test
    void Invalid_Account() {
        Account invalidAccount = new Account(subject1, "");
        assertThrows(AuthenticationException.class, () -> core.storeData(DATA, invalidAccount));
    }

    @Test
    void Null_Account() {
        assertThrows(NullPointerException.class, () -> core.storeData(DATA, null));
    }

    @Test
    void Null_Data() {
        assertThrows(NullPointerException.class, () -> core.storeData(null, account1));
    }

    @Test
    void Allow_Null_Subjects() {
        DataIdentifier identifier = assertDoesNotThrow(() -> core.storeData(DATA, account1, null));
        assertNotNull(identifier);
    }

    @Test
    void Allow_Empty_Subjects() {
        DataIdentifier identifier = assertDoesNotThrow(() -> core.storeData(DATA, account1, Collections.emptySet()));
        assertNotNull(identifier);
    }

    @Test
    void Allow_Null_Attachments() {
        Data nullAttachments = new Data(DATA.getData(), null);
        DataIdentifier identifier = assertDoesNotThrow(() -> core.storeData(nullAttachments, account1));
        assertNotNull(identifier);
    }

    @Test
    void Non_Existent_Attachments() {
        DataIdentifier invalidIdentifier = DataIdentifier.decode("0".repeat(64));
        Data nonExistentAttachment = new Data(DATA.getData(), Collections.singleton(invalidIdentifier));

        assertThrows(IllegalArgumentException.class, () -> core.storeData(nonExistentAttachment, account1));
    }

    @Test
    void Not_Authorized_Attachments() {
        DataIdentifier account1NotAuthorized = assertDoesNotThrow(() -> core.storeData(DATA, account2));
        assertNotNull(account1NotAuthorized);

        Data notAuthorizedAttachments = new Data(DATA.getData(), Collections.singleton(account1NotAuthorized));

        assertThrows(NotAuthorizedException.class, () -> core.storeData(notAuthorizedAttachments, account1));
    }

    @Test
    void Allow_Self_Addressed() {
        // Note: the account will implicitly be added to the recipients, this is not the intended method utilization
        DataIdentifier identifier = assertDoesNotThrow(() -> core.storeData(DATA, account1, Collections.singleton(subject1)));
        assertNotNull(identifier);
    }

    @Test
    void Store_Data_Multiple_Subjects() {
        DataIdentifier identifier = assertDoesNotThrow(() -> core.storeData(DATA, account1, Set.of(subject1, subject2)));
        assertNotNull(identifier);
    }

    @Test
    void Store_Data() {
        DataIdentifier identifier = assertDoesNotThrow(() -> core.storeData(DATA, account1));
        assertNotNull(identifier);
    }
}
