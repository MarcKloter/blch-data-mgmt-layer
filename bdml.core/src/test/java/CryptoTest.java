import bdml.core.domain.Capability;
import bdml.core.helper.Crypto;
import bdml.services.exceptions.MisconfigurationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CryptoTest {
    private Capability capability;
    private byte[] plainBytes1;
    private static final int IV_BYTES = 16;

    @BeforeAll
    public void setup() {
        String keyBase64 = "Vy4jawu/JPkG36pjCoEEGR2luox905qHyejhkFbXBj8=";
        byte[] key = Base64.getDecoder().decode(keyBase64);
        this.capability = new Capability(key);

        String plaintext = "The Magic Words are Squeamish Ossifrage";
        this.plainBytes1 = plaintext.getBytes(StandardCharsets.UTF_8);
    }

    @Test
    public void Symmetrically_Encryt_And_Decrypt() {
        byte[] cipherBytes = Crypto.symmetricallyEncrypt(capability, plainBytes1);
        byte[] decryptedBytes = Crypto.symmetricallyDecrypt(capability, cipherBytes);

        assertArrayEquals(plainBytes1, decryptedBytes);
    }

    @Test
    public void Symmetrically_Decrypt_Corrupt_Padding() {
        byte[] cipherBytes = Crypto.symmetricallyEncrypt(capability, plainBytes1);
        int size = cipherBytes.length;

        // swap last two bytes of ciphertext (cipherBytes = ciphertext || initialization vector)
        byte[] corruptCipherBytes = new byte[size];
        for(int i = 0; i < size; i++) {
            if(i == size - IV_BYTES - 2) corruptCipherBytes[i] = corruptCipherBytes[i+1];
            else if(i == size - IV_BYTES - 1) corruptCipherBytes[i] = corruptCipherBytes[i-1];
            else corruptCipherBytes[i] = cipherBytes[i];
        }

        assertThrows(MisconfigurationException.class, () -> Crypto.symmetricallyDecrypt(capability, corruptCipherBytes));
    }
}
