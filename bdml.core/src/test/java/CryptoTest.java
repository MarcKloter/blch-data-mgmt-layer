import bdml.core.domain.Capability;
import bdml.core.helper.Crypto;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

public class CryptoTest {
    @Test
    public void symmetricEncryption() {
        String keyBase64 = "Vy4jawu/JPkG36pjCoEEGR2luox905qHyejhkFbXBj8=";
        byte[] key = Base64.getDecoder().decode(keyBase64);
        Capability capability = new Capability(key);

        String plaintext = "The Magic Words are Squeamish Ossifrage";
        byte[] plainBytes = plaintext.getBytes(StandardCharsets.UTF_8);
        byte[] cipherBytes = Crypto.symmetricallyEncrypt(capability, plainBytes);
        byte[] decryptedBytes = Crypto.symmetricallyDecrypt(capability, cipherBytes);

        assertArrayEquals(plainBytes, decryptedBytes);
    }
}
