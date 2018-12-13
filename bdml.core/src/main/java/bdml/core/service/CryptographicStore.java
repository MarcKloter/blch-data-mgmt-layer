package bdml.core.service;

import java.security.Key;

public interface CryptographicStore {
	/**
	 * 
	 * @param cipher
	 * @param key
	 * @param plaintext
	 * @return
	 */
	public String encrypt(String cipher, Key key, String plaintext);

	/**
	 * 
	 * @param cipher
	 * @param key
	 * @param ciphertext
	 * @return
	 */
	public String decrypt(String cipher, Key key, String ciphertext);
}
