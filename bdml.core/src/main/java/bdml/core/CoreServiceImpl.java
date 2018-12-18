package bdml.core;

import java.util.List;

import bdml.services.api.types.Account;
import bdml.services.api.types.Filter;
import bdml.services.api.types.Subject;
import bdml.services.api.Core;

public class CoreServiceImpl implements Core {

	@Override
	public String storeData(String data, Account account, List<Subject> subjects, List<String> linking) {
		// TODO:
		return null;
	}

	@Override
	public List<String> listData(Account account, Filter filter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getData(String id, Account account) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Subject> listSubjects() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createAccount(String password) {
		// TODO Auto-generated method stub
		return null;
	}

}
