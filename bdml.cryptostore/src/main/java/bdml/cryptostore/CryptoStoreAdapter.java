package bdml.cryptostore;

import java.security.Key;
import bdml.core.service.CryptographicStore;

public class CryptoStoreAdapter implements CryptographicStore {

	@Override
	public String encrypt(String cipher, Key key, String plaintext) {
		// ECIESwithAES
		return null;
	}

	@Override
	public String decrypt(String cipher, Key key, String ciphertext) {
		// ECIESwithAES
		return null;
	}

}
