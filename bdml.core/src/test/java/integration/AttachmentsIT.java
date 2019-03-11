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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AttachmentsIT {
    private static final String PASSWORD_1 = "password1";
    private static final String PASSWORD_2 = "password2";
    private static final String PASSWORD_3 = "password3";

    private static final String DATA_STRING_1 = "data string one";
    private static final String DATA_STRING_2 = "data string two";
    private static final String DATA_STRING_3 = "data string three";

    private Core core;

    private Account account1;
    private Account account2;
    private Account account3;

    private Subject subject1;
    private Subject subject2;
    private Subject subject3;

    private DataIdentifier frameA;

    @BeforeAll
    void setup() {
        this.core = CoreService.getInstance();

        this.subject1 = core.createAccount(PASSWORD_1);
        this.account1 = new Account(subject1, PASSWORD_1);

        this.subject2 = core.createAccount(PASSWORD_2);
        this.account2 = new Account(subject2, PASSWORD_2);

        this.subject3 = core.createAccount(PASSWORD_3);
        this.account3 = new Account(subject3, PASSWORD_3);
    }

    @BeforeEach
    void prepare() throws AuthenticationException {
        this.frameA = core.storeData(new Data(DATA_STRING_1), account1);
    }

    @Test
    void Direct_Attachment() {
        // the creator of a frame A creates another frame B with A attached and sends this frame B to another person
        Data attachment = new Data(DATA_STRING_2, Collections.singleton(frameA));
        DataIdentifier frameB = assertDoesNotThrow(() -> core.storeData(attachment, account1, Collections.singleton(subject2)));

        Data result1 = assertDoesNotThrow(() -> core.getData(frameB, account2));
        assertNotNull(result1);
        assertEquals(result1.getData(), DATA_STRING_2);

        Data result2 = assertDoesNotThrow(() -> core.getData(frameA, account2));
        assertNotNull(result2);
        assertEquals(result2.getData(), DATA_STRING_1);
    }

    @Test
    void Indirect_Attachment() {
        // the creator of frame A creates B with A attached for subject 2.
        // subject 2 sends frame C with B attached to subject 3.
        Data attachment1 = new Data(DATA_STRING_2, Collections.singleton(frameA));
        DataIdentifier frameB = assertDoesNotThrow(() -> core.storeData(attachment1, account1, Collections.singleton(subject2)));

        Data result1 = assertDoesNotThrow(() -> core.getData(frameB, account2));
        assertNotNull(result1);
        assertEquals(result1.getData(), DATA_STRING_2);

        Data result2 = assertDoesNotThrow(() -> core.getData(frameA, account2));
        assertNotNull(result2);
        assertEquals(result2.getData(), DATA_STRING_1);

        Data attachment2 = new Data(DATA_STRING_3, Collections.singleton(frameB));
        DataIdentifier frameC = assertDoesNotThrow(() -> core.storeData(attachment2, account1, Collections.singleton(subject3)));

        Data result3 = assertDoesNotThrow(() -> core.getData(frameC, account3));
        assertNotNull(result3);
        assertEquals(result3.getData(), DATA_STRING_3);

        Data result4 = assertDoesNotThrow(() -> core.getData(frameB, account3));
        assertNotNull(result4);
        assertEquals(result4.getData(), DATA_STRING_2);

        Data result5 = assertDoesNotThrow(() -> core.getData(frameA, account3));
        assertNotNull(result5);
        assertEquals(result5.getData(), DATA_STRING_1);
    }

    @Test
    void Forward_Attachment() {
        // the creator of frame A creates B with A attached for subject 2.
        // subject 2 takes only frame A to create C with A attached for subject 3.
        Data attachment1 = new Data(DATA_STRING_2, Collections.singleton(frameA));
        DataIdentifier frameB = assertDoesNotThrow(() -> core.storeData(attachment1, account1, Collections.singleton(subject2)));

        Data result1 = assertDoesNotThrow(() -> core.getData(frameB, account2));
        assertNotNull(result1);
        assertEquals(result1.getData(), DATA_STRING_2);

        Data result2 = assertDoesNotThrow(() -> core.getData(frameA, account2));
        assertNotNull(result2);
        assertEquals(result2.getData(), DATA_STRING_1);

        Data attachment2 = new Data(DATA_STRING_3, Collections.singleton(frameA));
        DataIdentifier frameC = assertDoesNotThrow(() -> core.storeData(attachment2, account1, Collections.singleton(subject3)));

        Data result3 = assertDoesNotThrow(() -> core.getData(frameC, account3));
        assertNotNull(result3);
        assertEquals(result3.getData(), DATA_STRING_3);

        // subject 3 is supposed to only see frames C and A, not frame B:
        assertThrows(NotAuthorizedException.class, () -> core.getData(frameB, account3));

        Data result4 = assertDoesNotThrow(() -> core.getData(frameA, account3));
        assertNotNull(result4);
        assertEquals(result4.getData(), DATA_STRING_1);
    }
}
