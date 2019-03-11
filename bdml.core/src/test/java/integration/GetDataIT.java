package integration;

import bdml.core.Core;
import bdml.core.CoreService;
import bdml.core.PersonalCore;
import bdml.core.PersonalCoreService;
import bdml.core.domain.*;
import bdml.core.domain.exceptions.AuthenticationException;
import bdml.core.domain.exceptions.DataUnavailableException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GetDataIT {
    //TODO: working directory currently is bdml.core

    private static final String PASSWORD_1 = "password1";
    private static final String PASSWORD_2 = "password2";
    private static final String PASSWORD_3 = "password2";

    private static final RawData SIMPLE_DATA_1 = new RawData("data string 1");
    private static final RawData SIMPLE_DATA_2 = new RawData("data string 2");

    private Core core;

    private Account account1;
    private Account account2;
    private Account account3;

    private Subject subject1;
    private Subject subject2;
    private Subject subject3;

    private PersonalCore core1;
    private PersonalCore core2;
    private PersonalCore core3;


    private DataIdentifier identifier1;
    private DataIdentifier identifier2;

    @BeforeAll
    void setup() throws Exception {
        this.core = CoreService.getInstance();

        this.subject1 = core.createAccount(PASSWORD_1);
        this.account1 = new Account(subject1, PASSWORD_1);
        this.core1 = core.getPersonalService(account1);

        this.subject2 = core.createAccount(PASSWORD_2);
        this.account2 = new Account(subject2, PASSWORD_2);
        this.core2 = core.getPersonalService(account2);

        this.subject3 = core.createAccount(PASSWORD_3);
        this.account3 = new Account(subject3, PASSWORD_3);
        this.core3 = core.getPersonalService(account3);

        this.identifier1 = core1.storeData(SIMPLE_DATA_1);
        this.identifier2 = core2.storeData(SIMPLE_DATA_2);

        Awaiter.await(this.identifier1, core1);
        Awaiter.await(this.identifier2, core2);

    }

    @Test
    void Null_DataIdentifier() {
        assertThrows(NullPointerException.class, () -> core1.getData(null));
    }

    @Test
    void Invalid_DataIdentifier() {
        DataIdentifier invalidIdentifier = DataIdentifier.decode("0".repeat(64));
        assertThrows(DataUnavailableException.class, () -> core1.getData(invalidIdentifier));
    }

    @Test
    void Get_Data() {
        Data result1 = assertDoesNotThrow(() -> core1.getData(identifier1)).data;
        assertNotNull(result1);
        assertEquals( ((RawData) result1).getData(), SIMPLE_DATA_1.getData());

        Data result2 = assertDoesNotThrow(() -> core2.getData(identifier2)).data;
        assertNotNull(result2);
        assertEquals(((RawData) result2).getData(), SIMPLE_DATA_2.getData());
    }

    @Test
    void Get_NotAuthorized_Data() {
        assertThrows(DataUnavailableException.class,() -> core1.getData(identifier2));
        assertThrows(DataUnavailableException.class,() -> core2.getData(identifier1));
    }

    @Test
    void Get_Addressed_Data() throws Exception {
        DataIdentifier identifier = assertDoesNotThrow(() -> core1.storeData(SIMPLE_DATA_1));
        Awaiter.await(identifier, core1);
        assertDoesNotThrow(() -> core1.grantAccess(identifier, subject2));
        // assert that the account that called storeData can read the data
        Data result1 = assertDoesNotThrow(() -> core1.getData(identifier)).data;
        Awaiter.await(identifier, core2);
        assertNotNull(result1);
        assertEquals(((RawData) result1).getData(), SIMPLE_DATA_1.getData());



        // assert that the account that was given as subject can read the data
        Data result2 = assertDoesNotThrow(() -> core2.getData(identifier)).data;
        assertNotNull(result2);
        assertEquals(((RawData) result2).getData(), SIMPLE_DATA_1.getData());

        // assert that an unrelated account cannot read the data
        assertThrows(DataUnavailableException.class,() -> core3.getData(identifier));
    }

    @Test
    void Get_Data_With_Attachment() throws Exception {
        Set<DataIdentifier> attachment = Set.of(identifier1);
        Data dataWithAttachment = new RawData(SIMPLE_DATA_1.getData(), attachment);
        DataIdentifier identifier = assertDoesNotThrow(() -> core1.storeData(dataWithAttachment));
        assertNotNull(identifier);
        Awaiter.await(identifier, core1);
        Data result = assertDoesNotThrow(() -> core1.getData(identifier)).data;
        assertNotNull(result);
        assertEquals(((RawData) result).getData(), SIMPLE_DATA_1.getData());
        assertEquals(((RawData) result).getAttachments().size(), attachment.size());
        assertTrue(((RawData) result).getAttachments().contains(identifier1));
    }
}
