package bdml.services.api;

import bdml.services.api.exceptions.AuthenticationException;
import bdml.services.api.types.Account;
import bdml.services.api.types.Filter;

import java.util.List;

public interface Core {
	// TODO: javadoc

	String storeData(String data, Account account, List<String> subjects, List<String> linking) throws AuthenticationException;

	List<String> listData(Account account, Filter filter) throws AuthenticationException;

	String getData(String id, Account account) throws AuthenticationException;

	List<String> listSubjects();

	String createAccount(String password);
}
