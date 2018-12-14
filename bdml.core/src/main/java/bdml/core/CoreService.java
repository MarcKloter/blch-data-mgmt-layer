package bdml.core;

import java.util.List;

import bdml.core.objects.AccountObject;
import bdml.core.objects.FilterObject;
import bdml.core.objects.SubjectObject;

public interface CoreService {
	public String storeData(String data, AccountObject account, List<SubjectObject> subjects, List<String> linking);

	public List<String> listData(AccountObject account, FilterObject filter);

	public String getData(String id, AccountObject account);

	public List<SubjectObject> listSubjects();

	public String createAccount(String password);
}
