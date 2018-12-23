package bdml.core;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import bdml.blockchain.BlockchainAdapter;
import bdml.cryptostore.CryptoStoreAdapter;
import bdml.keyserver.KeyServerAdapter;
import bdml.services.Blockchain;
import bdml.services.CryptographicStore;
import bdml.services.KeyServer;
import bdml.services.api.exceptions.AuthenticationException;
import bdml.services.api.types.Account;
import bdml.services.api.types.Filter;
import bdml.services.api.types.Subject;
import bdml.services.api.Core;
import org.bouncycastle.util.encoders.Hex;

public class CoreImpl implements Core {
	private Blockchain blockchain = new BlockchainAdapter();
	private KeyServer keyServer = new KeyServerAdapter();
	private CryptographicStore cryptoStore = new CryptoStoreAdapter();

	@Override
	public String storeData(String data, Account account, List<String> subjects, List<String> linking) throws AuthenticationException {
		// check parameters
		Util.requireNonEmpty(data, "data");
		Util.requireNonNull(account, "account");
		Util.requireNonEmpty(subjects, "subjects");

		// TODO: implement

		if(false)
			throw new AuthenticationException();
		return null;
	}

	@Override
	public List<String> listData(Account account, Filter filter) throws AuthenticationException {
		// check parameters
		Objects.requireNonNull(account, "Parameter 'account' cannot be null.");

		// TODO: implement
		if(false)
			throw new AuthenticationException();
		return null;
	}

	@Override
	public String getData(String id, Account account) throws AuthenticationException {
		// check parameters
		Util.requireNonEmpty(id, "id");
		Util.requireNonNull(account, "account");

		// TODO: implement
		if(false)
			throw new AuthenticationException();
		return null;
	}

	@Override
	public List<Subject> listSubjects() {
		// TODO: implement
		List<Subject> result = new ArrayList<>();
		// result.add(new Subject());
		return result;
	}

	@Override
	public String createAccount(String password) {
		// check parameters
		Util.requireNonEmpty(password, "password");

		// create key pair for the account
		PublicKey publicKey = cryptoStore.generateKeyPair(password);

		// take 20 LSB in hex representation as account identifier
		byte[] pkBytes = publicKey.getEncoded();
		byte[] idBytes = Arrays.copyOfRange(pkBytes, pkBytes.length - 20, pkBytes.length);
		String identifier = Hex.toHexString(idBytes);

		// pass created public key to the key server for distribution
		keyServer.registerKey(identifier, publicKey);

		// TODO: handle unimplemented keyserver

		// create account to use
		blockchain.createAccount(password);

		return identifier;
	}

}
