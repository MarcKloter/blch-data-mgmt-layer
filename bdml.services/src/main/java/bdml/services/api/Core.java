package bdml.services.api;

import bdml.services.api.exceptions.AuthenticationException;
import bdml.services.api.exceptions.NotAuthorizedException;
import bdml.services.api.types.Account;
import bdml.services.api.types.Data;
import bdml.services.api.types.Identifier;

import java.util.List;
import java.util.Set;

public interface Core {
	// TODO: javadoc

	/**
	 *
	 * @param data
	 * @param attachments
	 * @param account
	 * @param subjects
	 * @return
	 * @throws NotAuthorizedException if {@code attachments} cannot be accessed by {@code account}.
	 * @throws AuthenticationException
	 */
	String storeData(String data, List<String> attachments, Account account, List<String> subjects) throws AuthenticationException;
	String storeData(String data, Account account, List<String> subjects) throws AuthenticationException;
	String storeData(String data, Account account) throws AuthenticationException;
	String storeData(Data data, Account account, List<String> subjects) throws AuthenticationException;
	String storeData(Data data, Account account) throws AuthenticationException;

	/**
	 *
	 * TODO: does not list attachments "directly accessible"
	 * @param account
	 * @return
	 * @throws AuthenticationException
	 */
	Set<String> listData(Account account) throws AuthenticationException;

	/**
	 * Polling method that returns a list of identifiers of accessible data by the given account since last poll.
	 * TODO: does not list attachments "directly accessible"
	 *
	 * @param account
	 * @return
	 * @throws AuthenticationException
	 */
	Set<String> listDataChanges(Account account) throws AuthenticationException;

	Identifier listAttachments(Account account, String identifier) throws AuthenticationException;

	/**
	 *
	 * @param id
	 * @param account
	 * @return {@link Data} object identified by {@code id} or {@code null}.
	 * @throws NotAuthorizedException if the data cannot be accessed by {@code account}.
	 * @throws AuthenticationException
	 */
	Data getData(String id, Account account) throws AuthenticationException;

	/**
	 *
	 * @param password
	 * @return
	 */
	String createAccount(String password);
}
