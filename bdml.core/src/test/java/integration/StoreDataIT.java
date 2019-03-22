package integration;

import bdml.core.Core;
import bdml.core.CoreService;
import bdml.core.PersonalCore;
import bdml.core.domain.*;
import bdml.core.domain.exceptions.AuthenticationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StoreDataIT {
    //TODO: working directory currently is bdml.core

    private static final String PASSWORD_1 = "password1";
    private static final String PASSWORD_2 = "password2";

    private static final RawData DATA = new RawData("data string");

    private Core core;

    private Account account1;
    private Account account2;

    private Subject subject1;
    private Subject subject2;

    private PersonalCore core1;
    private PersonalCore core2;

    @BeforeAll
    void setup() throws AuthenticationException {
        this.core = CoreService.getInstance();

        this.subject1 = core.createAccount(PASSWORD_1);
        this.account1 = new Account(subject1, PASSWORD_1);
        this.core1 = core.getPersonalService(account1);

        this.subject2 = core.createAccount(PASSWORD_2);
        this.account2 = new Account(subject2, PASSWORD_2);
        this.core2 = core.getPersonalService(account2);
    }

    @Test
    void Invalid_Account() {
        Account invalidAccount = new Account(subject1, "");
        assertThrows(AuthenticationException.class, () -> core.getPersonalService(invalidAccount));
    }

    @Test
    void Null_Data() {
        assertThrows(NullPointerException.class, () -> core1.storeData(null));
    }

    @Test
    void Allow_Null_Attachments() {
        Data nullAttachments = new RawData(DATA.getData(), null);
        DataIdentifier identifier = assertDoesNotThrow(() -> core1.storeData(nullAttachments));
        assertNotNull(identifier);
    }

    @Test
    void Non_Existent_Attachments() {
        DataIdentifier invalidIdentifier = DataIdentifier.decode("0".repeat(64));
        Data nonExistentAttachment = new RawData(DATA.getData(), Collections.singleton(invalidIdentifier));

        assertNull(assertDoesNotThrow( () -> core1.storeData(nonExistentAttachment)));
    }

    @Test
    void Not_Authorized_Attachments() throws Exception {
        DataIdentifier account1NotAuthorized = assertDoesNotThrow(() -> core2.storeData(DATA));
        assertNotNull(account1NotAuthorized);
        Awaiter.awaitData(account1NotAuthorized, core2);
        Data notAuthorizedAttachments = new RawData(DATA.getData(), Collections.singleton(account1NotAuthorized));
        assertNull(assertDoesNotThrow(() -> core1.storeData(notAuthorizedAttachments)));
    }

    @Test
    void Allow_Self_Addressed() throws Exception {
        // Note: the account will implicitly be added to the recipients, this is not the intended method utilization
        DataIdentifier identifier = assertDoesNotThrow(() -> core1.storeData(DATA));
        assertNotNull(identifier);
        Awaiter.awaitData(identifier, core1);
        assertDoesNotThrow(() -> core1.grantAccess(identifier,subject1));
    }

    @Test
    void Store_Data_Multiple_Subjects() throws Exception {
        DataIdentifier identifier = assertDoesNotThrow(() -> core1.storeData(DATA));
        assertNotNull(identifier);
        Awaiter.awaitData(identifier, core1);
        assertDoesNotThrow(() -> core1.grantAccess(identifier, subject1));
        assertDoesNotThrow(() -> core1.grantAccess(identifier, subject2));
        Awaiter.awaitData(identifier, core2);
    }

    @Test
    void Store_Data() {
        DataIdentifier identifier = assertDoesNotThrow(() -> core1.storeData(DATA));
        assertNotNull(identifier);
    }

    @Test
    void Store_Amend() throws Exception {
        DataIdentifier identifier = assertDoesNotThrow(() -> core1.storeData(DATA));
        assertNotNull(identifier);
        Awaiter.awaitData(identifier, core1);
        DataIdentifier identifier2 = assertDoesNotThrow(() -> core1.storeData(DATA));
        assertNotNull(identifier2);
        Awaiter.awaitData(identifier2, core1);
        assertDoesNotThrow(() -> core1.amendDocument(identifier,identifier2));
    }
}
