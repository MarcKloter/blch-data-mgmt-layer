package bdml.cryptostore.mapping;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.encoders.Hex;

public class Dummy {

	public static void main(String[] args) throws FileNotFoundException, NoSuchAlgorithmException,
			NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException,
			IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException, IOException {
		Security.addProvider(new BouncyCastleProvider());

		// WINDOWS
		String keyPath = "C:/Users/Marc/AppData/Roaming/Parity/Ethereum/keys/DevelopmentChain/";
		String keyFile = "UTC--2018-11-27T10-15-41Z--372bb0b4-38ed-12a4-f3a8-500e8074fc38";

		// MAC
		//String keyPath = "/Users/marckloter/Library/Application Support/io.parity.ethereum/keys/DevelopmentChain/";
		//String keyFile = "UTC--2018-12-12T10-17-01Z--d4ab078c-3af7-7c21-a97f-19fee234d32a";

		String password = "myPassword";

		ObjectMapper mapper = new ObjectMapper();
		File file = new File(keyPath + keyFile);
		System.out.println(file.exists());
		KeystoreFile data = mapper.readValue(new FileReader(file), KeystoreFile.class);
		System.out.println(data.crypto.ciphertext);
		System.out.println(data.crypto.cipher);

		byte[] salt = Hex.decode(data.crypto.kdfparams.salt);
		int iterations = data.crypto.kdfparams.c;
		int dklen = data.crypto.kdfparams.dklen;

		// encrypt password
		KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, dklen * 8);

		SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

		byte[] derivedKey = skf.generateSecret(spec).getEncoded();
		byte[] encryptedPassword = Arrays.copyOfRange(derivedKey, 0, 16);
		byte[] macInput = Arrays.copyOfRange(derivedKey, 16, 32);
		byte[] ciphertext = Hex.decode(data.crypto.ciphertext);

		// TODO: MAC == Keccak256(macInput + ciphertext)
		byte[] mac = Hex.decode(data.crypto.mac);
		MessageDigest digest = MessageDigest.getInstance("KECCAK-256", "BC");
		byte[] macRes = digest.digest(Arrays.concatenate(macInput, ciphertext));
		System.out.println("HMAC: " + (Arrays.areEqual(mac, macRes)));

		// TODO: decrypt private key
		byte[] iv = Hex.decode(data.crypto.cipherparams.iv);
		SecretKeySpec key = new SecretKeySpec(encryptedPassword, "AES");
		Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding", "BC");
		cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
		byte[] plaintext = cipher.doFinal(ciphertext);

		System.out.println("private key: " + Hex.toHexString(plaintext));

		// Private Key: 0d97187680dabaf3179d434973c8ad902b7c128fda1bdda1d1aae19279101a1d
	}
}
