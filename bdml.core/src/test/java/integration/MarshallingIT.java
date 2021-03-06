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

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MarshallingIT {
    private static final String PASSWORD_1 = "password1";
    private static final String PASSWORD_2 = "password2";

    private static final Data DATA = new Data("data string");
    private static final DataIdentifier IDENTIFIER = DataIdentifier.decode("2149406ec775b60d2d88ac170947a6bc458b0e0dca860eedd122bf9950ca1779");

    private Core core;

    private Account account1;
    private Account account2;

    private Subject subject1;
    private Subject subject2;

    private DataIdentifier blockchainFrame;

    @BeforeAll
    void setup() throws AuthenticationException {
        this.core = CoreService.getInstance();

        this.subject1 = core.createAccount(PASSWORD_1);
        this.account1 = new Account(subject1, PASSWORD_1);

        this.subject2 = core.createAccount(PASSWORD_2);
        this.account2 = new Account(subject2, PASSWORD_2);

        this.blockchainFrame = core.storeData(DATA, account1);
    }

    @Test
    void Invalid_Account() {
        Account invalidAccount = new Account(subject1, "");
        assertThrows(AuthenticationException.class, () -> core.marshalFrame(DATA, invalidAccount));
        assertThrows(AuthenticationException.class, () -> core.unmarshalFrame(IDENTIFIER, new byte[0], invalidAccount));
    }

    @Test
    void Null_Params() {
        assertThrows(NullPointerException.class, () -> core.marshalFrame(null, account1));
        assertThrows(NullPointerException.class, () -> core.marshalFrame(DATA, null));

        assertThrows(NullPointerException.class, () -> core.unmarshalFrame(null, new byte[0], account1));
        assertThrows(NullPointerException.class, () -> core.unmarshalFrame(IDENTIFIER, null, account1));
        assertThrows(NullPointerException.class, () -> core.unmarshalFrame(IDENTIFIER, new byte[0], null));
    }

    @Test
    void Marshal_On_Chain_Attachment() {
        Data dataWithAttachment = new Data(DATA.getData(), Set.of(blockchainFrame));
        Map.Entry<DataIdentifier, byte[]> marshalledFrame = assertDoesNotThrow(() -> core.marshalFrame(dataWithAttachment, account1));
        assertNotNull(marshalledFrame.getKey());
        assertNotNull(marshalledFrame.getValue());

        Data unmarshalledFrame = assertDoesNotThrow(() -> core.unmarshalFrame(marshalledFrame.getKey(), marshalledFrame.getValue(), account1));
        assertNotNull(unmarshalledFrame);
        assertEquals(unmarshalledFrame.getData(), DATA.getData());
        assertEquals(unmarshalledFrame.getAttachments().size(), dataWithAttachment.getAttachments().size());
        assertTrue(unmarshalledFrame.getAttachments().contains(blockchainFrame));
    }

    @Test
    void Marshal_Not_Authorized_Attachment() {
        // account1 stored the frame on the blockchain, account2 is not authorized to attach it
        Data dataWithAttachment = new Data(DATA.getData(), Set.of(blockchainFrame));
        NotAuthorizedException exception = assertThrows(NotAuthorizedException.class, () -> core.marshalFrame(dataWithAttachment, account2));
        System.out.println(String.format("Exception message: %s", exception.getMessage()));
    }

    @Test
    void Marshal_Off_Chain_Attachment() {
        // same as a non existent attachment (as it does not exist on the blockchain)
        Map.Entry<DataIdentifier, byte[]> marshalledFrame = assertDoesNotThrow(() -> core.marshalFrame(DATA, account1));
        assertNotNull(marshalledFrame.getKey());
        Data dataWithAttachment = new Data(DATA.getData(), Set.of(marshalledFrame.getKey()));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> core.marshalFrame(dataWithAttachment, account1));
        System.out.println(String.format("Exception message: %s", exception.getMessage()));
    }

    @Test
    void Unmarshal_Invalid_Frame() {
        assertThrows(IllegalArgumentException.class, () -> core.unmarshalFrame(IDENTIFIER, new byte[0], account1));
    }

    @Test
    void Marshal_And_Unmarshal() {
        Map.Entry<DataIdentifier, byte[]> marshalledFrame = assertDoesNotThrow(() -> core.marshalFrame(DATA, account1));
        assertNotNull(marshalledFrame.getKey());
        assertNotNull(marshalledFrame.getValue());

        Data unmarshalledFrame = assertDoesNotThrow(() -> core.unmarshalFrame(marshalledFrame.getKey(), marshalledFrame.getValue(), account1));
        assertNotNull(unmarshalledFrame);
        assertEquals(unmarshalledFrame.getData(), DATA.getData());
    }

    @Test
    void Unmarshal_Not_Authorized() {
        Map.Entry<DataIdentifier, byte[]> marshalledFrame = assertDoesNotThrow(() -> core.marshalFrame(DATA, account1));
        assertNotNull(marshalledFrame.getKey());
        assertNotNull(marshalledFrame.getValue());

        assertThrows(NotAuthorizedException.class, () -> core.unmarshalFrame(marshalledFrame.getKey(), marshalledFrame.getValue(), account2));
    }
}
