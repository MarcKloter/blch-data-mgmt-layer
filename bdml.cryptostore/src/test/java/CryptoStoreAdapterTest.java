import bdml.cryptostore.CryptoStoreAdapter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * As the CryptoStoreAdapter is the mock of a cryptographic storage, focus on happy path testing.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CryptoStoreAdapterTest {
    private static final String SECRET = "GA3k8fcxYRVwP4w8";

    private CryptoStoreAdapter cryptoStore;
    private byte[] plainBytes1;

    @BeforeAll
    void setup() {
        Properties configuration = new Properties();
        configuration.put("bdml.output.directory", "test-data");
        this.cryptoStore = new CryptoStoreAdapter(configuration);

        String plaintext = "The Magic Words are Squeamish Ossifrage";
        this.plainBytes1 = plaintext.getBytes(StandardCharsets.UTF_8);
    }

    @Test
    void Generate_KeyPair() {
        PublicKey publicKey = cryptoStore.generateKeyPair(SECRET);
        assertNotNull(publicKey);
    }

    @Test
    void Check_Wrong_Secret() {
        PublicKey publicKey = cryptoStore.generateKeyPair(SECRET);
        assertFalse(cryptoStore.checkKeyPair(publicKey, ""));
    }

    @Test
    void Check_Null() {
        assertFalse(cryptoStore.checkKeyPair(null, null));
    }

    @Test
    void Check_Existent_Key() {
        PublicKey publicKey = cryptoStore.generateKeyPair(SECRET);
        assertTrue(cryptoStore.checkKeyPair(publicKey, SECRET));
    }

    @Test
    void Encrypt_And_Decrypt() {
        PublicKey publicKey = cryptoStore.generateKeyPair(SECRET);

        byte[] cipherBytes = cryptoStore.encrypt(publicKey, plainBytes1);
        byte[] decryptedBytes = cryptoStore.decrypt(publicKey, SECRET, cipherBytes);

        assertArrayEquals(plainBytes1, decryptedBytes);
    }

    @Test
    void Decrypt_Wrong_Key() {
        PublicKey publicKey1 = cryptoStore.generateKeyPair(SECRET);
        PublicKey publicKey2 = cryptoStore.generateKeyPair(SECRET);

        byte[] cipherBytes = cryptoStore.encrypt(publicKey1, plainBytes1);
        byte[] decryptedBytes = cryptoStore.decrypt(publicKey2, SECRET, cipherBytes);

        assertNull(decryptedBytes);
    }

    @Test
    void Decrypt_Wrong_Secret() {
        PublicKey publicKey = cryptoStore.generateKeyPair(SECRET);

        byte[] cipherBytes = cryptoStore.encrypt(publicKey, plainBytes1);
        byte[] decryptedBytes = cryptoStore.decrypt(publicKey, "", cipherBytes);

        assertNull(decryptedBytes);
    }
}
