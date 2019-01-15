package bdml.services.api;

import bdml.services.api.exceptions.AuthenticationException;
import bdml.services.api.exceptions.NotAuthorizedException;
import bdml.services.api.helper.DataListener;
import bdml.services.api.types.Account;
import bdml.services.api.types.Data;
import bdml.services.api.types.Identifier;
import java.util.Set;

public interface Core {
	/**
	 * Creates a new account to store and receive data as, secured by the given password.
	 *
	 * @param password password for the new account
	 * @return 20 bytes identifier of the new account in hex string representation.
	 */
	String createAccount(String password);

	/**
	 * Stores data in the connected blockchain.
	 * The {@link Account#getIdentifier()} of {@code account} is implicitly added to the {@code subjects}.
	 * The data is being encrypted using the public keys related to the {@code subjects}.
	 *
	 * @param data Data object to store
	 * @param account {@link Account} to interact as
	 * @param subjects set of 20 byte account identifiers to authorize.
	 * @return 32 bytes data identifier in hex string representation.
	 * @throws NotAuthorizedException if {@code attachments} cannot be accessed by {@code account}.
	 * @throws AuthenticationException if the {@link Account#getIdentifier()} and {@link Account#getPassword()}
	 * combination do not correspond to an existing account.
	 */
	String storeData(Data data, Account account, Set<String> subjects) throws AuthenticationException;

	/**
	 * Stores plain data without attachments
	 * {@code data} is set to {@code new RawData(data,null)}.
	 * @see Core#storeData(Data, Account, Set)
	 */
	String storeData(String data, Account account, Set<String> subjects) throws AuthenticationException;

	/**
	 * Stores plain personal data without attachments
	 * Only the creator can retrieve the data
	 * {@code data} is set to {@code new RawData(data,null)}.
	 * {@code subject} is set to {@code null}.
	 * @see Core#storeData(Data, Account, Set)
	 */
	String storeData(String data, Account account) throws AuthenticationException;

	/**
	 *{@code data} is set to {@code new RawData(data,attachments)}.
	 * @see Core#storeData(String, Set, Account, Set)
	 */
	String storeData(String data, Set<String> attachments, Account account, Set<String> subjects) throws AuthenticationException;

	/**
	 * {@code subjects} defaults to {@code null}.
	 *
	 * @see Core#storeData(String, Set, Account, Set)
	 */
	String storeData(Data data, Account account) throws AuthenticationException;

	/**
	 * Lists the data identifiers of data that can directly be accessed by {@code account}.
	 * Direct accessible data are entries where the {@link Account#getIdentifier()} of {@code account} is either creator or recipient of.
	 * Attachments will not be included in the result.
	 *
	 * @param account {@link Account} to interact as
	 * @return Set of 32 byte data identifiers in hex string representation.
	 * @throws AuthenticationException if the {@link Account#getIdentifier()} and {@link Account#getPassword()}
	 * combination do not correspond to an existing account.
	 */
	Set<String> listData(Account account) throws AuthenticationException;

	/**
	 * Lists data identifiers of data that can directly be accessed by the provided account since the last time this method was called.
	 * Direct accessible data are entries where the {@link Account#getIdentifier()} of {@code account} is either creator or recipient of.
	 * Attachments will not be included in the result.
	 *
	 * @param account {@link Account} to interact as
	 * @return Set of 32 byte data identifiers in hex string representation.
	 * @throws AuthenticationException if the {@link Account#getIdentifier()} and {@link Account#getPassword()}
	 * combination do not correspond to an existing account.
	 */
	Set<String> listDataChanges(Account account) throws AuthenticationException;

	/**
	 * Lists the data identifiers of the referenced entry {@code id} and all attachments recursively.
	 *
	 * @param id 32 byte data identifier in hex string representation
	 * @param account {@link Account} to interact as
	 * @return {@link Identifier} object that recursively contains all linked attachments {@link Identifier#getAttachments()}.
	 * @throws AuthenticationException if the {@link Account#getIdentifier()} and {@link Account#getPassword()}
	 * combination do not correspond to an existing account.
	 */
	Identifier listAttachments(String id, Account account) throws AuthenticationException;

	/**
	 * Returns data stored in the connected blockchain referenced by {@code id}.
	 *
	 * @param id 32 byte data identifier in hex string representation
	 * @param account {@link Account} to interact as
	 * @return {@link Data} object identified by {@code id} or {@code null}.
	 * @throws NotAuthorizedException if the data referenced by {@code id} cannot be accessed by {@code account}.
	 * @throws AuthenticationException if the {@link Account#getIdentifier()} and {@link Account#getPassword()}
	 * combination do not correspond to an existing account.
	 */
	Data getData(String id, Account account) throws AuthenticationException;

	/**
	 * Registers the given {@link DataListener} for {@code account}.
	 * The {@link DataListener#update(String)} method will be called whenever the connected blockchain client received
	 * data where {@code account} was a recipient of.
	 *
	 * @param account {@link Account} to interact as
	 * @param dataListener {@link DataListener} object to subscribe
	 * @return 8 bytes subscription handle in hex string representation.
	 * @throws AuthenticationException if the {@link Account#getIdentifier()} and {@link Account#getPassword()}
	 * combination do not correspond to an existing account.
	 */
	String registerDataListener(Account account, DataListener dataListener) throws AuthenticationException;

	/**
	 * Unregisters the previously registered {@link DataListener} of {@code account} under {@code handle}.
	 *
	 * @param account {@link Account} to interact as
	 * @param handle 8 bytes subscription handle in hex string representation
	 * @throws AuthenticationException if the {@link Account#getIdentifier()} and {@link Account#getPassword()}
	 * combination do not correspond to an existing account.
	 */
	void unregisterDataListener(Account account, String handle) throws AuthenticationException;
}
