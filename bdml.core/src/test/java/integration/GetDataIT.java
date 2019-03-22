package integration;

import bdml.core.Core;
import bdml.core.CoreService;
import bdml.core.PersonalCore;
import bdml.core.domain.*;
import bdml.core.domain.exceptions.DataUnavailableException;
import bdml.core.persistence.Payload;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GetDataIT {
    //TODO: working directory currently is bdml.core

    private static final String PASSWORD_1 = "password1";
    private static final String PASSWORD_2 = "password2";
    private static final String PASSWORD_3 = "password2";

    private static final RawData SIMPLE_DATA_1 = new RawData("data string 1");
    private static final RawData SIMPLE_DATA_2 = new RawData("data string 2");
    private static final RawPayload SIMPLE_DATA_PLAIN = new RawPayload("plain data string");


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
    private DataIdentifier identifierPlain;


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
        this.identifierPlain = core.publishData(SIMPLE_DATA_PLAIN);


        Awaiter.awaitData(this.identifier1, core1);
        Awaiter.awaitData(this.identifier2, core2);
        Awaiter.awaitData(this.identifierPlain, core3);
    }

    @Test
    void Null_DataIdentifier() {
        assertThrows(NullPointerException.class, () -> core1.getData(null, true));
    }

    @Test
    void Invalid_DataIdentifier() {
        DataIdentifier invalidIdentifier = DataIdentifier.decode("0".repeat(64));
        assertThrows(DataUnavailableException.class, () -> core1.getData(invalidIdentifier, true));
    }

    @Test
    void Get_Data() {
        Data result1 = assertDoesNotThrow(() -> core1.getData(identifier1, true)).data;
        assertNotNull(result1);
        assertEquals( ((RawData) result1).getData(), SIMPLE_DATA_1.getData());

        Data result2 = assertDoesNotThrow(() -> core2.getData(identifier2, true)).data;
        assertNotNull(result2);
        assertEquals(((RawData) result2).getData(), SIMPLE_DATA_2.getData());
    }

    @Test
    void Get_Data_Public() {

        Payload resultPlain = assertDoesNotThrow(() -> core.getPublicData(identifierPlain)).data;
        assertNotNull(resultPlain);
        assertEquals(((RawPayload) resultPlain).getData(), SIMPLE_DATA_PLAIN.getData());

        Data resultPlain1 = assertDoesNotThrow(() -> core1.getData(identifierPlain, true)).data;
        assertNotNull(resultPlain1);
        assertEquals(((RawData) resultPlain1).getData(), SIMPLE_DATA_PLAIN.getData());

        Data resultPlain2 = assertDoesNotThrow(() -> core2.getData(identifierPlain, true)).data;
        assertNotNull(resultPlain2);
        assertEquals(((RawData) resultPlain2).getData(), SIMPLE_DATA_PLAIN.getData());

        Data resultPlain3 = assertDoesNotThrow(() -> core3.getData(identifierPlain, true)).data;
        assertNotNull(resultPlain3);
        assertEquals(((RawData) resultPlain3).getData(), SIMPLE_DATA_PLAIN.getData());
    }


    @Test
    void Get_Data_Direct() {
        Capability cap1  = assertDoesNotThrow(() -> core1.exportCapability(identifier1, true).data);
        assertNotNull(cap1);

        Payload result1 = assertDoesNotThrow(() -> core.getDataDirect(cap1)).data;
        assertNotNull(result1);
        assertEquals( ((RawPayload) result1).getData(), SIMPLE_DATA_1.getData());

        Capability cap2  = assertDoesNotThrow(() -> core2.exportCapability(identifier2, true).data);
        assertNotNull(cap2);

        Payload result2 = assertDoesNotThrow(() -> core.getDataDirect(cap2)).data;
        assertNotNull(result2);
        assertEquals(((RawPayload) result2).getData(), SIMPLE_DATA_2.getData());



    }

    @Test
    void Get_Plain_As_Cap() {
        assertThrows(DataUnavailableException.class,() -> core1.exportCapability(identifierPlain, true));
        assertThrows(DataUnavailableException.class,() -> core2.exportCapability(identifierPlain, true));
        assertThrows(DataUnavailableException.class,() -> core3.exportCapability(identifierPlain, true));
    }

    @Test
    void Get_NotAuthorized_Data() {
        assertThrows(DataUnavailableException.class,() -> core1.getData(identifier2, true));
        assertThrows(DataUnavailableException.class,() -> core2.getData(identifier1, true));
    }

    @Test
    void Grant_Plain_Data() {
        assertThrows(DataUnavailableException.class,() -> core1.grantAccess(identifierPlain, subject1));
        assertThrows(DataUnavailableException.class,() -> core1.grantAccess(identifierPlain, subject2));
        assertThrows(DataUnavailableException.class,() -> core1.grantAccess(identifierPlain, subject3));
        assertThrows(DataUnavailableException.class,() -> core2.grantAccess(identifierPlain, subject1));
        assertThrows(DataUnavailableException.class,() -> core2.grantAccess(identifierPlain, subject2));
        assertThrows(DataUnavailableException.class,() -> core2.grantAccess(identifierPlain, subject3));
        assertThrows(DataUnavailableException.class,() -> core3.grantAccess(identifierPlain, subject1));
        assertThrows(DataUnavailableException.class,() -> core3.grantAccess(identifierPlain, subject2));
        assertThrows(DataUnavailableException.class,() -> core3.grantAccess(identifierPlain, subject3));
    }

    @Test
    void Get_Addressed_Data() throws Exception {
        DataIdentifier identifier = assertDoesNotThrow(() -> core1.storeData(SIMPLE_DATA_1));
        Awaiter.awaitData(identifier, core1);

        assertDoesNotThrow(() -> core1.grantAccess(identifier, subject2));
        // assert that the account that called storeData can read the data
        Data result1 = assertDoesNotThrow(() -> core1.getData(identifier, true)).data;
        Awaiter.awaitData(identifier, core2);
        assertNotNull(result1);
        assertEquals(((RawData) result1).getData(), SIMPLE_DATA_1.getData());


        // assert that the account that was given as subject can read the data
        Data result2 = assertDoesNotThrow(() -> core2.getData(identifier, true)).data;
        assertNotNull(result2);
        assertEquals(((RawData) result2).getData(), SIMPLE_DATA_1.getData());

        // assert that an unrelated account cannot read the data
        assertThrows(DataUnavailableException.class,() -> core3.getData(identifier, true));
    }



    @Test
    void Get_Data_With_Attachment() throws Exception {
        DataIdentifier identifierA = assertDoesNotThrow(() -> core1.storeData(SIMPLE_DATA_1));
        Awaiter.awaitData(identifierA, core1);

        Set<DataIdentifier> attachment = Set.of(identifierA);
        Data dataWithAttachment = new RawData(SIMPLE_DATA_1.getData(), attachment);
        DataIdentifier identifier = assertDoesNotThrow(() -> core1.storeData(dataWithAttachment));
        assertNotNull(identifier);
        Awaiter.awaitData(identifier, core1);

        Data resultA = assertDoesNotThrow(() -> core1.getData(identifierA, true)).data;
        assertNotNull(resultA);


        Data result = assertDoesNotThrow(() -> core1.getData(identifier, true)).data;
        assertNotNull(result);
        assertEquals(((RawData) result).getData(), SIMPLE_DATA_1.getData());
        assertEquals(((RawData) result).getAttachments().size(), attachment.size());
        assertTrue(((RawData) result).getAttachments().contains(identifierA));

        Set<DataIdentifier> attach = core1.listAttachmentsToData(identifier, true);
        assertNotNull(attach);
        assertTrue(!attach.isEmpty());
        assertTrue(attach.contains(identifierA));

    }

    @Test
    void Grant_Data_Over_Attachment() throws Exception {
        DataIdentifier identifierA = assertDoesNotThrow(() -> core1.storeData(SIMPLE_DATA_1));
        Awaiter.awaitData(identifierA, core1);


        Set<DataIdentifier> attachment = Set.of(identifierA);
        Data dataWithAttachment = new RawData(SIMPLE_DATA_1.getData(), attachment);
        DataIdentifier identifier = assertDoesNotThrow(() -> core1.storeData(dataWithAttachment));
        assertNotNull(identifier);
        Awaiter.awaitData(identifier, core1);


        assertDoesNotThrow(() -> core1.grantAccess(identifier, subject2));
        Awaiter.awaitData(identifier, core2);

        Data resultA = assertDoesNotThrow(() -> core2.getData(identifierA, true)).data;
        assertNotNull(resultA);

        Data result = assertDoesNotThrow(() -> core2.getData(identifier, true)).data;
        assertNotNull(result);
        assertEquals(((RawData) result).getData(), SIMPLE_DATA_1.getData());
        assertEquals(((RawData) result).getAttachments().size(), attachment.size());
        assertTrue(((RawData) result).getAttachments().contains(identifierA));

        assertThrows(DataUnavailableException.class,() -> core3.getData(identifierA, true));
        assertThrows(DataUnavailableException.class,() -> core3.getData(identifier, true));
    }

    @Test
    void Grant_Data_Over_Attachment_Direct() throws Exception {
        DataIdentifier identifierA = assertDoesNotThrow(() -> core1.storeData(SIMPLE_DATA_1));
        Awaiter.awaitData(identifierA, core1);


        Set<DataIdentifier> attachment = Set.of(identifierA);
        Data dataWithAttachment = new RawData(SIMPLE_DATA_1.getData(), attachment);
        DataIdentifier identifier = assertDoesNotThrow(() -> core1.storeData(dataWithAttachment));
        assertNotNull(identifier);
        Awaiter.awaitData(identifier, core1);

        assertDoesNotThrow(() -> core1.grantAccess(identifier, subject2));
        Awaiter.awaitData(identifier, core2);

        Capability cap = assertDoesNotThrow(() -> core2.exportCapability(identifier, true)).data;
        assertNotNull(cap);

        Payload result = assertDoesNotThrow(() -> core.getDataDirect(cap)).data;
        assertNotNull(result);
        assertEquals(((RawPayload) result).getData(), SIMPLE_DATA_1.getData());

        Capability capA = ((RawPayload)result).getAttachments().iterator().next();

        Payload resultA = assertDoesNotThrow(() -> core.getDataDirect(capA)).data;
        assertNotNull(resultA);
        assertEquals(((RawPayload) resultA).getData(), SIMPLE_DATA_1.getData());

    }


    @Test
    void Get_Data_With_Amendment() throws Exception {
        DataIdentifier identifier = assertDoesNotThrow(() -> core1.storeData(SIMPLE_DATA_1));
        assertNotNull(identifier);
        Awaiter.awaitData(identifier, core1);

        DataIdentifier identifierA = assertDoesNotThrow(() -> core1.storeData(SIMPLE_DATA_1));
        assertNotNull(identifierA);
        Awaiter.awaitData(identifierA, core1);

        assertDoesNotThrow(() -> core1.amendDocument(identifier, identifierA));
        Set<DataIdentifier> amends = core1.listAmendmentsToData(identifier, true);

        assertNotNull(amends);
        assertTrue(!amends.isEmpty());
        assertTrue(amends.contains(identifierA));

    }


    @Test
    void Get_Data_With_Amendment_Then_Attachement() throws Exception {
        DataIdentifier identifier = assertDoesNotThrow(() -> core1.storeData(SIMPLE_DATA_1));
        assertNotNull(identifier);
        Awaiter.awaitData(identifier, core1);

        DataIdentifier identifierA0 = assertDoesNotThrow(() -> core1.storeData(SIMPLE_DATA_2));
        assertNotNull(identifierA0);
        Awaiter.awaitData(identifierA0, core1);


        Set<DataIdentifier> attachment = Set.of(identifierA0);
        Data dataWithAttachment = new RawData(SIMPLE_DATA_1.getData(), attachment);
        DataIdentifier identifierA1 = assertDoesNotThrow(() -> core1.storeData(dataWithAttachment));
        assertNotNull(identifierA1);
        Awaiter.awaitData(identifierA1, core1);

        assertDoesNotThrow(() -> core1.amendDocument(identifier, identifierA1));
        assertDoesNotThrow(() -> core1.grantAccess(identifier, subject2));

        Awaiter.awaitData(identifierA0, core2);

        Data result0 = assertDoesNotThrow(() -> core2.getData(identifierA0, true)).data;
        assertNotNull(result0);
        assertEquals(((RawData) result0).getData(), SIMPLE_DATA_2.getData());

        Data result1 = assertDoesNotThrow(() -> core2.getData(identifierA1, true)).data;
        assertNotNull(result1);
        assertEquals(((RawData) result1).getData(), SIMPLE_DATA_1.getData());
        assertEquals(((RawData) result1).getAttachments().size(), attachment.size());
        assertTrue(((RawData) result1).getAttachments().contains(identifierA0));
    }

    @Test
    void Get_Data_With_Attachement_Then_Amendment() throws Exception {
        DataIdentifier identifier = assertDoesNotThrow(() -> core1.storeData(SIMPLE_DATA_1));
        assertNotNull(identifier);
        Awaiter.awaitData(identifier, core1);

        DataIdentifier identifierA0 = assertDoesNotThrow(() -> core1.storeData(SIMPLE_DATA_2));
        assertNotNull(identifierA0);
        Awaiter.awaitData(identifierA0, core1);


        Set<DataIdentifier> attachment = Set.of(identifierA0);
        Data dataWithAttachment = new RawData(SIMPLE_DATA_1.getData(), attachment);
        DataIdentifier identifierA1 = assertDoesNotThrow(() -> core1.storeData(dataWithAttachment));
        assertNotNull(identifierA1);
        Awaiter.awaitData(identifierA1, core1);

        assertDoesNotThrow(() -> core1.amendDocument(identifierA0, identifier));
        assertDoesNotThrow(() -> core1.grantAccess(identifierA1, subject2));

        Awaiter.awaitData(identifier, core2);

        Data result = assertDoesNotThrow(() -> core2.getData(identifier, true)).data;
        assertNotNull(result);
        assertEquals(((RawData) result).getData(), SIMPLE_DATA_1.getData());

        Data result0 = assertDoesNotThrow(() -> core2.getData(identifierA0, true)).data;
        assertNotNull(result0);
        assertEquals(((RawData) result0).getData(), SIMPLE_DATA_2.getData());

        Data result1 = assertDoesNotThrow(() -> core2.getData(identifierA1, true)).data;
        assertNotNull(result1);
        assertEquals(((RawData) result1).getData(), SIMPLE_DATA_1.getData());
        assertEquals(((RawData) result1).getAttachments().size(), attachment.size());
        assertTrue(((RawData) result1).getAttachments().contains(identifierA0));
    }

}
