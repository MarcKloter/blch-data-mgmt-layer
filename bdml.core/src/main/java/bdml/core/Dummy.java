package bdml.core;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.Security;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

public class Dummy {
	public static void main(String[] args) throws GeneralSecurityException {
		Security.addProvider(new BouncyCastleProvider());

		String ipfsHash = "QmYA2fn8cMbVWo4v95RwcwJVyQsNtnEwHerfWR8UNtEwoE";
		// keccak256 hash of ipfs hash
		String capString = "572e236b0bbf24f906dfaa630a8104191da5ba8c7dd39a87c9e8e19056d7063f";

		// cap = hash(plaintext)
		MessageDigest digest = MessageDigest.getInstance("KECCAK-256", "BC");
		byte[] cap = digest.digest(ipfsHash.getBytes());
		System.out.println(Hex.toHexString(cap)); // 572e236b0bbf24f906dfaa630a8104191da5ba8c7dd39a87c9e8e19056d7063f

		byte[] capBytes = Hex.decode(capString);
		System.out.println(capBytes.length);

		// TODO: AES in config
		SecretKeySpec key = new SecretKeySpec(capBytes, "AES");

		// TODO: provider string in config
		// AES/EBC/PKCS7Padding AES/CBC/PKCS7Padding
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		byte[] ciphertext = cipher.doFinal(ipfsHash.getBytes());
		System.out.println(Hex.toHexString(ciphertext));

		// TODO: decryption
	}
}
