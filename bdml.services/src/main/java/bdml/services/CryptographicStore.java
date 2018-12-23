package bdml.services;

import java.security.PublicKey;

public interface CryptographicStore {
	// TODO: javadoc

	/**
	 * Generates a public/private key pair secured by the provided secret.
	 * Future operations requiring the private key will require to provide this secret.
	 * The algorithm used must be defined by the implementation.
	 *
	 * @param secret
	 * @return
	 */
	PublicKey generateKeyPair(String secret);

	/**
	 *
	 * The cipher used must be defined by the implementation.
	 *
	 * @param key
	 * @param plaintext
	 * @return
	 */
	String encrypt(PublicKey key, String plaintext);

	/**
	 *
	 * The cipher used must be defined by the implementation.
	 *
	 * @param key
	 * @param secret
	 * @param ciphertext
	 * @return
	 */
	String decrypt(PublicKey key, String secret, String ciphertext);
}
