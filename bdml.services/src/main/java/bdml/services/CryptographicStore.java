package bdml.services;

import java.security.Key;

public interface CryptographicStore {
	// TODO: javadoc

	String encrypt(String cipher, Key key, String plaintext);

	String decrypt(String cipher, Key key, String ciphertext);
}
