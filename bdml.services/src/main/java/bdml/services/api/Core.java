package bdml.services.api;

import bdml.services.api.exceptions.AuthenticationException;
import bdml.services.api.types.Account;
import bdml.services.api.types.Data;
import bdml.services.api.types.Filter;

import java.util.List;
import java.util.Optional;

public interface Core {
	// TODO: javadoc

	String storeData(String data, List<String> attachments, Account account, List<String> subjects) throws AuthenticationException;
	String storeData(String data, Account account, List<String> subjects) throws AuthenticationException;
	String storeData(String data, Account account) throws AuthenticationException;
	String storeData(Data data, Account account, List<String> subjects) throws AuthenticationException;
	String storeData(Data data, Account account) throws AuthenticationException;

	List<String> listData(Account account, Filter filter) throws AuthenticationException;

	Data getData(String id, Account account) throws AuthenticationException;

	List<String> listSubjects();

	String createAccount(String password);
}
