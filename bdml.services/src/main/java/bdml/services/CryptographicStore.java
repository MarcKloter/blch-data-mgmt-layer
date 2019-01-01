package bdml.services;

import java.security.PublicKey;

public interface CryptographicStore {
	// TODO: javadoc

	/**
	 * Generates a public/private key pair secured by the provided secret.
	 * Future operations requiring the private key will require to provide this secret.
	 * The algorithm used must be defined by the implementation.
	 *
	 * @param secret password to the key pair
	 * @return Public key of the generated key pair.
	 */
	PublicKey generateKeyPair(String secret);

	/**
	 * Checks whether there is a private key for the given public key secured by the provided secret.
	 *
	 * @param publicKey public key to check
	 * @param secret password to the given public key
	 * @return Boolean whether the combination of public key and secret exist.
	 */
	boolean checkKeyPair(PublicKey publicKey, String secret);

	/**
	 *
	 * The cipher used must be defined by the implementation.
	 *
	 * @param key TODO
	 * @param plaintext TODO
	 * @return TODO
	 */
	byte[] encrypt(PublicKey key, byte[] plaintext);

	/**
	 *
	 * The cipher used must be defined by the implementation.
	 *
	 * @param key TODO
	 * @param secret TODO
	 * @param ciphertext TODO
	 * @return TODO
	 */
	byte[] decrypt(PublicKey key, String secret, byte[] ciphertext);
}
