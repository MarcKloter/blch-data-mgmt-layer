package bdml.cryptostore;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.*;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

import bdml.cryptostore.persistence.SecuredKeyPair;
import bdml.services.CryptographicStore;
import bdml.services.exceptions.MisconfigurationException;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import static org.bouncycastle.util.encoders.Hex.toHexString;

public class CryptoStoreAdapter implements CryptographicStore {
	private final String KEY_GEN_ALG = "EC";
	private final String FILENAME = "keyPairs.json";

	private List<SecuredKeyPair> keyPairs;

	public CryptoStoreAdapter() {
		// load previously generated key pairs
		ObjectMapper mapper = new ObjectMapper();
		File file = new File(FILENAME);
		if(file.exists()) {
			try {
				JsonParser jsonParser = new JsonFactory().createParser(file);
				CollectionType valueType = mapper.getTypeFactory().constructCollectionType(List.class, SecuredKeyPair.class);
				this.keyPairs = mapper.readValue(jsonParser, valueType);
			} catch (IOException e) {
				throw new MisconfigurationException(e.getMessage());
			}
		} else {
			this.keyPairs = new ArrayList<>();
		}
	}

	@Override
	public PublicKey generateKeyPair(String secret) {
		Security.addProvider(new BouncyCastleProvider());

		// generate key pair
		KeyPairGenerator gen;
		try {
			gen = KeyPairGenerator.getInstance(KEY_GEN_ALG);
		} catch (NoSuchAlgorithmException e) {
			throw new MisconfigurationException(e.getMessage());
		}
		KeyPair keyPair = gen.generateKeyPair();

		// persist generated key pair
		String publicKey = toHexString(keyPair.getPublic().getEncoded());
		String privateKey = toHexString(keyPair.getPrivate().getEncoded());
		String passwordHash = Util.sha256(secret);
		this.keyPairs.add(new SecuredKeyPair(publicKey, privateKey, passwordHash));

		ObjectMapper mapper = new ObjectMapper();
		try {
			mapper.writeValue(new FileWriter(FILENAME, false), keyPairs);
		} catch (IOException e) {
			throw new MisconfigurationException(e.getMessage());
		}

		return keyPair.getPublic();
	}

	@Override
	public String encrypt(PublicKey key, String plaintext) {
		// ECIESwithAES
		return null;
	}

	@Override
	public String decrypt(PublicKey key, String secret, String ciphertext) {
		// ECIESwithAES
		return null;
	}

}
