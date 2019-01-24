import bdml.keyserver.KeyServerAdapter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Properties;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * As the KeyServerAdapter is the mock of a key server connected to eg. a PKI, focus on happy path testing.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KeyServerAdapterTest {
    private KeyServerAdapter keyServer;

    private PublicKey publicKeyEC;
    private String identifier;

    @BeforeAll
    void setup() {
        Properties configuration = new Properties();
        configuration.put("bdml.output.directory", "test-data");
        this.keyServer = new KeyServerAdapter(configuration);

        byte[] array = new byte[20];
        new Random().nextBytes(array);
        this.identifier = new String(array, StandardCharsets.UTF_8);

        try {
            this.publicKeyEC = generateECKeyPair().getPublic();
        } catch (NoSuchAlgorithmException e) {
            fail();
        }
    }

    private KeyPair generateECKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("EC");
        return gen.generateKeyPair();
    }

    @Test
    void Register_And_Query() {
        assertDoesNotThrow(() -> keyServer.registerKey(identifier, publicKeyEC));
        assertNotNull(keyServer.queryKey(identifier));
    }

    @Test
    void Register_Identifier_Null() {
        assertThrows(NullPointerException.class, () -> keyServer.registerKey(null, publicKeyEC));
    }

    @Test
    void Register_Key_Null() {
        assertThrows(NullPointerException.class, () -> keyServer.registerKey(identifier, null));
    }

    @Test
    void Query_Null() {
        assertNull(keyServer.queryKey(null));
    }
}
