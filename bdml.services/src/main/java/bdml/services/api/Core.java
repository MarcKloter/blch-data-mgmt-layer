package bdml.services.api;

import bdml.services.api.types.Account;
import bdml.services.api.types.Filter;
import bdml.services.api.types.Subject;

import java.util.List;

public interface Core {
	// TODO: javadoc

	String storeData(String data, Account account, List<Subject> subjects, List<String> linking);

	List<String> listData(Account account, Filter filter);

	String getData(String id, Account account);

	List<Subject> listSubjects();

	String createAccount(String password);
}
