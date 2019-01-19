import bdml.core.domain.Capability;
import bdml.core.helper.Crypto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CryptoTest {
    private Capability capability1;
    private Capability capability2;
    private byte[] plainBytes1;
    private static final int IV_BYTES = 16;

    @BeforeAll
    void setup() {
        byte[] key1 = Base64.getDecoder().decode("Vy4jawu/JPkG36pjCoEEGR2luox905qHyejhkFbXBj8=");
        this.capability1 = new Capability(key1);

        byte[] key2 = Base64.getDecoder().decode("dMoSbsneUmQntUtkikCTsj0I/2vBI+1aOR84PgAld+4=");
        this.capability2 = new Capability(key2);

        String plaintext = "The Magic Words are Squeamish Ossifrage";
        this.plainBytes1 = plaintext.getBytes(StandardCharsets.UTF_8);
    }

    @Test
    void Symmetrically_Encryt_And_Decrypt() {
        byte[] cipherBytes = Crypto.symmetricallyEncrypt(capability1, plainBytes1);
        byte[] decryptedBytes = Crypto.symmetricallyDecrypt(capability1, cipherBytes);

        assertArrayEquals(plainBytes1, decryptedBytes);
    }

    @Test
    void Symmetrically_Decrypt_Wrong_Key() {
        byte[] cipherBytes = Crypto.symmetricallyEncrypt(capability1, plainBytes1);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> Crypto.symmetricallyDecrypt(capability2, cipherBytes));
        System.out.println(String.format("Exception message: %s", exception.getMessage()));
    }

    @Test
    void Symmetrically_Decrypt_Corrupt_Padding() {
        byte[] cipherBytes = Crypto.symmetricallyEncrypt(capability1, plainBytes1);
        int size = cipherBytes.length;

        // swap last two bytes of ciphertext (cipherBytes = ciphertext || initialization vector)
        byte[] corruptCipherBytes = new byte[size];
        for(int i = 0; i < size; i++) {
            if(i == size - IV_BYTES - 2) corruptCipherBytes[i] = corruptCipherBytes[i+1];
            else if(i == size - IV_BYTES - 1) corruptCipherBytes[i] = corruptCipherBytes[i-1];
            else corruptCipherBytes[i] = cipherBytes[i];
        }

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> Crypto.symmetricallyDecrypt(capability1, corruptCipherBytes));
        System.out.println(String.format("Exception message: %s", exception.getMessage()));
    }
}
