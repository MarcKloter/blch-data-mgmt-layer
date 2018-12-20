package bdml.core;

import java.util.ArrayList;
import java.util.List;

import bdml.services.api.exceptions.AuthenticationException;
import bdml.services.api.types.Account;
import bdml.services.api.types.Filter;
import bdml.services.api.types.Subject;
import bdml.services.api.Core;

public class CoreImpl implements Core {

	@Override
	public String storeData(String data, Account account, List<String> subjects, List<String> linking) throws AuthenticationException {
		if(data.isEmpty())
			throw new IllegalArgumentException("Parameter 'data' cannot be empty.");
		if(account == null)
			throw new IllegalArgumentException("Parameter 'account' cannot be null.");
		if(subjects.size() == 0)
			throw new IllegalArgumentException("No subject provided.");

		System.out.println("storeData called");
		String result = "572e236b0bbf24f906dfaa630a8104191da5ba8c7dd39a87c9e8e19056d7063f";

		if(false)
			throw new AuthenticationException();
		return null;
	}

	@Override
	public List<String> listData(Account account, Filter filter) throws AuthenticationException {
		// TODO Auto-generated method stub
		if(false)
			throw new AuthenticationException();
		return null;
	}

	@Override
	public String getData(String id, Account account) throws AuthenticationException {
		// TODO Auto-generated method stub
		if(false)
			throw new AuthenticationException();
		return null;
	}

	@Override
	public List<Subject> listSubjects() {
		List<Subject> result = new ArrayList<>();
		// result.add(new Subject());
		return result;
	}

	@Override
	public String createAccount(String password) {
		// TODO Auto-generated method stub
		return "test";
	}

}
