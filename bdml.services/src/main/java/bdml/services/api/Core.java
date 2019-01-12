package bdml.services.api;

import bdml.services.api.exceptions.AuthenticationException;
import bdml.services.api.exceptions.NotAuthorizedException;
import bdml.services.api.helper.DataListener;
import bdml.services.api.types.Account;
import bdml.services.api.types.Data;
import bdml.services.api.types.Identifier;
import java.util.Set;

public interface Core {
	// TODO: javadoc

	/**
	 *
	 * @param password
	 * @return
	 */
	String createAccount(String password);

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
	String storeData(String data, Set<String> attachments, Account account, Set<String> subjects) throws AuthenticationException;

	/**
	 * {@code attachments} defaults to {@code null}.
	 *
	 * @see Core#storeData(String, Set, Account, Set)
	 */
	String storeData(String data, Account account, Set<String> subjects) throws AuthenticationException;

	/**
	 * {@code attachments} defaults to {@code null}.
	 * {@code subjects} defaults to {@code null}.
	 *
	 * @see Core#storeData(String, Set, Account, Set)
	 */
	String storeData(String data, Account account) throws AuthenticationException;

	/**
	 * {@code data} is set to {@link Data#getData()}.
	 * {@code attachments} is set to {@link Data#getAttachments()}.
	 *
	 * @see Core#storeData(String, Set, Account, Set)
	 */
	String storeData(Data data, Account account, Set<String> subjects) throws AuthenticationException;

	/**
	 * {@code data} is set to {@link Data#getData()}.
	 * {@code attachments} is set to {@link Data#getAttachments()}.
	 * {@code subjects} defaults to {@code null}.
	 *
	 * @see Core#storeData(String, Set, Account, Set)
	 */
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

	String registerDataListener(Account account, DataListener dataListener) throws AuthenticationException;

	void unregisterDataListener(Account account, String handle) throws AuthenticationException;
}
