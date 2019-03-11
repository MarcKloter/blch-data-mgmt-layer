package bdml.core;

import bdml.core.domain.*;
import bdml.core.domain.exceptions.DataUnavailableException;
import bdml.core.persistence.Payload;
import bdml.services.QueryResult;

import java.util.Set;

/**
 * PersonalCore represents the view of a specific subject on to the Datastore
 * This class is bound to an account
 */
public interface PersonalCore {


	/**
	 * Get the subject of the account this instance is bound to
	 * @return {@link Subject} of this instance
	 */
	Subject getActiveAccount();

	/**
	 * Persists data in the connected blockchain.
	 * The data is being encrypted and a volatile capability to read it is added.
	 *  volatile capabilities are not restored if the node is reset and resynced from the chain
	 *  A volatile capability can be made permanent in two ways:
	 *  	1. attach it to a document where you have a permanent capability
	 *  	2. call {@code grantAccess} to give yourself permanent access
	 *  If the data links to other data over their capability the other data becomes available to everybody that gets access to this data
	 * @param data {@link Data} to persist
	 * @return {@link DataIdentifier} of the persisted data, or
	 * 			{@code null} if the data was invalid (@see Payload.isValid())
	 * @throws NullPointerException if {@code data} is {@code null}.
	 */
	DataIdentifier storeData(Data data);

	/**
	 * Grants permanent access for subject to id.
	 * This requires that the active account has access to id
	 * @param id {@link DataIdentifier} to grant access to
	 * @param subject {@link Subject} to grant access for
	 * @throws NullPointerException if {@code data} or {@code null} is {@code null}.
	 * @throws DataUnavailableException if {@code id} does not exist or the current account has no access to it.
	 */
	void grantAccess(DataIdentifier id, Subject subject) throws DataUnavailableException;


	/**
	 * Designates the document amendment as an update to the document original
	 * This requires that the active account has access to the original and the amendment
	 * This grants access to the amendment to subjects that have access to the original
	 * @param original {@link DataIdentifier} to amend
	 * @param amendment {@link DataIdentifier} representing the update
	 * @throws NullPointerException if {@code original} or {@code amendment} is {@code null}.
	 * @throws DataUnavailableException if {@code original} or {@code amendment} does not exist or the current account has no access to it.
	 */
	void amendDocument(DataIdentifier original, DataIdentifier amendment) throws DataUnavailableException;

	/**
	 * Persists data in the connected blockchain.
	 * The data is unencrypted and everybody can read it.
	 *  If the data links to other data over their capability the other data becomes available to everybody as well
	 * @param data {@link Data} to persist
	 * @return {@link DataIdentifier} of the persisted data, or
	 * 			{@code null} if the data was invalid (@see Payload.isValid())
	 * @throws NullPointerException if {@code data} is {@code null}.
	 */
	DataIdentifier publishData(Data data);

	/**
	 * Returns data stored in the connected blockchain referenced by {@code id}.
	 *
	 * @param id {@link DataIdentifier} of the data to retrieve
	 * @return {@link Data} object identified by {@code id} or {@code null}.
	 * @throws NullPointerException if {@code id} or {@code account} is {@code null}.
	 * @throws DataUnavailableException if {@code id} does not exist or the current account has no access to it.
	 */
	QueryResult<Data> getData(DataIdentifier id) throws DataUnavailableException;


	/**
	 * Returns a set of {@link DataIdentifier} containing the identifier of earthing that became accessible since this was called last
	 *
	 * @return {@link Set<DataIdentifier>} newly accessible documents.
	 */
	Set<DataIdentifier> listNewDocuments();

	/**
	 * Lists all amendment data identifiers to the document identified by identifier.
	 *
	 * @param identifier {@link DataIdentifier} of the document for which to retrieve amendments
	 * @return {@link Set<DataIdentifier>} data identifier that are amendments to identifier.
	 * @throws NullPointerException if {@code identifier} is {@code null}.
	 */
	Set<DataIdentifier> listAmendmentsToData(DataIdentifier identifier);

	/**
	 * Lists all linked data identifiers to the document identified by identifier.
	 *
	 * @param identifier {@link DataIdentifier} of the document for which to retrieve links
	 * @return {@link Set<DataIdentifier>} data identifier that are linked from identifier.
	 * @throws NullPointerException if {@code identifier} is {@code null}.
	 */
	Set<DataIdentifier> listAttachmentsToData(DataIdentifier identifier);

}
