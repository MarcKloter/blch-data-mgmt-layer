package bdml.core;

import bdml.core.domain.*;
import bdml.core.domain.exceptions.AuthenticationException;
import bdml.core.domain.exceptions.NotAuthorizedException;

import java.security.PublicKey;
import java.util.Map;
import java.util.Set;

public interface Core {
	/**
	 * Creates a new account to store and receive data as, secured by the given password.
	 *
	 * @param password password for the new account
	 * @return {@link Subject} of the new account.
	 * @throws IllegalArgumentException if {@code password} is empty.
	 * @throws NullPointerException if {@code password} is {@code null}.
	 */
	Subject createAccount(String password);

	String exportSubject(Subject subject);
	Subject importSubject(String pk);

	/**
	 * Persists data in the connected blockchain.
	 * The {@link Account#getIdentifier()} of {@code account} is implicitly added to the {@code subjects}.
	 * The data is being encrypted using the public keys related to the {@code subjects}.
	 *
	 * @param data {@link Data} to persist
	 * @param account {@link Account} to interact as
	 * @param subjects set of {@link Subject} to authorize, can be {@code null}
	 * @return {@link DataIdentifier} of the persisted data.
	 * @throws AuthenticationException if the {@link Account#getIdentifier()} and {@link Account#getPassword()}
	 * combination do not correspond to an existing account.
	 * @throws NotAuthorizedException if any {@link Data#getAttachments()} of {@code data} cannot be accessed by {@code account}.
	 * @throws IllegalArgumentException if any {@link Data#getAttachments()} of {@code data} does not exist.
	 * @throws NullPointerException if {@code data} or {@code account} is {@code null}.
	 */
	DataIdentifier storeData(Data data, Account account, Set<Subject> subjects) throws AuthenticationException;

	/**
	 * {@code subjects} defaults to {@code null}.
	 *
	 * @see Core#storeData(Data, Account, Set)
	 */
	DataIdentifier storeData(Data data, Account account) throws AuthenticationException;

	/**
	 * Lists the data identifiers of data that can directly be accessed by {@code account}.
	 * Direct accessible data are entries where the {@link Account#getIdentifier()} of {@code account} is either creator or recipient of.
	 * Attachments will not be included in the result.
	 *
	 * @param account {@link Account} to interact as
	 * @return Set of {@link DataIdentifier}.
	 * @throws AuthenticationException if the {@link Account#getIdentifier()} and {@link Account#getPassword()}
	 * combination do not correspond to an existing account.
	 * @throws NullPointerException if {@code account} is {@code null}.
	 */
	Set<DataIdentifier> listDirectlyAccessibleData(Account account) throws AuthenticationException;

	/**
	 * Lists data identifiers of data that can directly be accessed by the provided account since the last time this method was called.
	 * Direct accessible data are entries where the {@link Account#getIdentifier()} of {@code account} is either creator or recipient of.
	 * Attachments will not be included in the result.
	 *
	 * @param account {@link Account} to interact as
	 * @return Set of {@link DataIdentifier}.
	 * @throws AuthenticationException if the {@link Account#getIdentifier()} and {@link Account#getPassword()}
	 * combination do not correspond to an existing account.
	 * @throws NullPointerException if {@code account} is {@code null}.
	 */
	Set<DataIdentifier> listDirectlyAccessibleDataChanges(Account account) throws AuthenticationException;

	/**
	 * Returns the {@link TreeNode} to the data identified by {@code id} which contains a hierarchical tree structure of
	 * all attachments.
	 *
	 * @param id {@link DataIdentifier} to list the attachments for
	 * @param account {@link Account} to interact as
	 * @return {@link TreeNode<DataIdentifier>} object that hierarchically contains all linked attachments.
	 * @throws AuthenticationException if the {@link Account#getIdentifier()} and {@link Account#getPassword()}
	 * combination do not correspond to an existing account.
	 * @throws IllegalArgumentException if there was no data found identified by {@code identifier}.
	 * @throws NullPointerException if {@code id} or {@code account} is {@code null}.
	 */
	TreeNode<DataIdentifier> listAttachments(DataIdentifier id, Account account) throws AuthenticationException;

	/**
	 * Returns data stored in the connected blockchain referenced by {@code id}.
	 *
	 * @param id {@link DataIdentifier} of the data to retrieve
	 * @param account {@link Account} to interact as
	 * @return {@link Data} object identified by {@code id} or {@code null}.
	 * @throws AuthenticationException if the {@link Account#getIdentifier()} and {@link Account#getPassword()}
	 * combination do not correspond to an existing account.
	 * @throws NotAuthorizedException if the data referenced by {@code id} cannot be accessed by {@code account}.
	 * @throws IllegalArgumentException if there was no data found identified by {@code identifier}.
	 * @throws NullPointerException if {@code id} or {@code account} is {@code null}.
	 */
	Data getData(DataIdentifier id, Account account) throws AuthenticationException;

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
	 * @throws NullPointerException if {@code account} or {@code dataListener} is {@code null}.
	 */
	String registerDataListener(Account account, DataListener dataListener) throws AuthenticationException;

	/**
	 * Unregisters the previously registered {@link DataListener} of {@code account} under {@code handle}.
	 *
	 * @param account {@link Account} to interact as
	 * @param handle 8 bytes subscription handle in hex string representation
	 * @throws AuthenticationException if the {@link Account#getIdentifier()} and {@link Account#getPassword()}
	 * combination do not correspond to an existing account.
	 * @throws NullPointerException if {@code account} or {@code handle} is {@code null}.
	 */
	void unregisterDataListener(Account account, String handle) throws AuthenticationException;


	/**
	 * Marshals the given {@code data} into a serialized frame addressed to the {@code subjects}.
	 * Frames marshaled through this method will not be cached and cannot be referenced in on-chain entries as attachments.
	 * Attaching data through {@link Data#getAttachments()} is not allowed.
 	 * The {@link Account#getIdentifier()} of {@code account} is implicitly added to the {@code subjects}.
	 * The data is being encrypted using the public keys related to the {@code subjects}.
	 *
	 * @param data {@link Data} to marshal, {@link Data#getAttachments()} must be {@code null} or empty
	 * @param account {@link Account} to interact as
	 * @param subjects set of {@link Subject} to authorize, can be {@code null}
	 * @return Mapping of {@link DataIdentifier} to the serialized frame.
	 * @throws AuthenticationException if the {@link Account#getIdentifier()} and {@link Account#getPassword()}
	 * combination do not correspond to an existing account.
	 * @throws IllegalArgumentException if any {@link Data#getAttachments()} are supplied.
	 * @throws NullPointerException if {@code data} or {@code account} is {@code null}.
	 */
	Map.Entry<DataIdentifier, byte[]> marshalFrame(Data data, Account account, Set<Subject> subjects) throws AuthenticationException;

	/**
	 * {@code subjects} defaults to {@code null}.
	 *
	 * @see Core#marshalFrame(Data, Account, Set)
	 */
	Map.Entry<DataIdentifier, byte[]> marshalFrame(Data data, Account account) throws AuthenticationException;

	/**
	 * Unmarshals the given {@code frame} using the provided {@code account}.
	 *
	 * @param identifier {@link DataIdentifier} corresponding to the serialized {@code frame} to assert the ID = H(CAP) property
	 * @param frame serialized frame to unmarshal
	 * @param account {@link Account} to interact as
	 * @return {@link Data} object unmarshaled from {@code frame}.
	 * @throws AuthenticationException  if the {@link Account#getIdentifier()} and {@link Account#getPassword()}
	 * combination do not correspond to an existing account.
	 * @throws NotAuthorizedException if the {@code frame} cannot be accessed by {@code account}.
	 * @throws IllegalArgumentException if the {@code frame} is invalid.
	 * @throws NullPointerException if {@code id}, {@code frame} or {@code account} is {@code null}.
	 */
	Data unmarshalFrame(DataIdentifier identifier, byte[] frame, Account account) throws AuthenticationException;


	//Alternative to marshall unmarshall but hands out cap (Needed for now)
	Map.Entry<Capability, byte[]> selfEncrypt(byte[] data);
	byte[] selfDecrypt(Capability key, byte[] data);
	//FOR DEMO
	byte[] raw(DataIdentifier identifier);

}
