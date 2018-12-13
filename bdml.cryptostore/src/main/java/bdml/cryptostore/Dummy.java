package bdml.cryptostore;

import java.security.*;

import javax.crypto.Cipher;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.IESParameterSpec;
import org.bouncycastle.util.encoders.Hex;

public class Dummy {
	public static void main(String[] args) throws GeneralSecurityException {
		KeyPair kp = generateKeyPair();
		PublicKey pk = kp.getPublic();
		System.out.println("Public Key: " + pk.toString());
		String cap = "ee8e01eff7acd538e8f6e6deea1a971e1be920ee4ceb4419434315dac04ed736";
//		String cap = "8e";
		byte[] plaintext = Hex.decode(cap);
		System.out.println("cap bits: " + plaintext.length * 8);
		byte[] ciphertext = encrypt(pk, plaintext);
		System.out.println(ciphertext.length);
		System.out.println(Hex.encode(ciphertext));
	}
	
	public static byte[] encrypt(Key publicKey, byte[] plaintext) throws GeneralSecurityException {
		Security.addProvider(new BouncyCastleProvider());
		
		// ECIESWITHAES-CBC (AES CBC) --> does not work in this example
		// ECIES (AES ECB)
		// TODO: random number used only ONCE
		byte[] nonce = Hex.decode("000102030405060708090a0b0c0d0e0f");
		Cipher iesCipher = Cipher.getInstance("ECIESwithAES-CBC", "BC");
		IESParameterSpec params = new IESParameterSpec(null, null, 256, 256, nonce, true);	
//		Cipher iesCipher = Cipher.getInstance("ECIES", "BC");
//		IESParameterSpec params = new IESParameterSpec(null, null, 256, 256, null, true);
		iesCipher.init(Cipher.ENCRYPT_MODE, publicKey, params);
		byte[] ciphertext = iesCipher.doFinal(plaintext);
		
//		 byte[] derivation = hexStringToByteArray("202122232425262728292a2b2c2d2e2f");
//		 byte[] encoding = hexStringToByteArray("303132333435363738393a3b3c3d3e3f");
//		 byte[] nonce = hexStringToByteArray("000102030405060708090a0b0c0d0e0f");
//		 
//		 Cipher c = Cipher.getInstance("ECIESwithAES-CBC", "BC");
//		 IESParameterSpec params = new IESParameterSpec(derivation, encoding, 128, 128, nonce, true);
//		 c.init(Cipher.ENCRYPT_MODE, publicKey, params);
//		 byte[] ciphertext = c.doFinal(plaintext);

		// 0x04 || coordinate x || coordinate y || (PKCS5 padded) ciphertext || 20-byte HMAC-digest
		// 0x02/0x03 || coordinate x || (PKCS5 padded) ciphertext || 20-byte HMAC-digest
		return ciphertext;
	}
	
	public static KeyPair generateKeyPair() throws GeneralSecurityException {
		Security.addProvider(new BouncyCastleProvider());

		KeyPairGenerator gen = KeyPairGenerator.getInstance("EC");
		KeyPair keyPair = gen.generateKeyPair();

		return keyPair;
	}
}
