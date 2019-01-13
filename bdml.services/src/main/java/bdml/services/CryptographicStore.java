package bdml.services;

import java.security.PublicKey;

public interface CryptographicStore {
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
	 * Asymmetrically encrypts the given {@code plaintext} using the public key {@code key}.
	 * The cipher used must be defined by the implementation.
	 *
	 * @param key {@link PublicKey} to encrypt
	 * @param plaintext byte array containing a plaintext
	 * @return Byte array containing the encrypted plaintext (= ciphertext).
	 */
	byte[] encrypt(PublicKey key, byte[] plaintext);

	/**
	 * Asymmetrically decrypts the given {@code ciphertext} using the private key corresponding to the {@code key} and
	 * {@code secret} combination.
	 * The cipher used must be defined by the implementation.
	 *
	 * @param key public key corresponding to a key pair
	 * @param secret secret corresponding to the given public key
	 * @param ciphertext byte array containing a ciphertext
	 * @return Byte array containing the decrypted ciphertext (= plaintext) or {@code null}.
	 */
	byte[] decrypt(PublicKey key, String secret, byte[] ciphertext);
}
