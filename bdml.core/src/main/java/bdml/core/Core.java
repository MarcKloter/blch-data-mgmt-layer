package bdml.core;

import bdml.core.domain.*;
import bdml.core.domain.exceptions.AuthenticationException;
import bdml.core.domain.exceptions.DataUnavailableException;
import bdml.core.persistence.Payload;
import bdml.services.Blockchain;
import bdml.services.QueryResult;
import java.util.Map;

public interface Core {

    /**
     * Returns data stored in the connected blockchain referenced by {@code cap}.
     * Does not need an account (allows to hand over caps to externals that have no account)
     *
     * @param cap {@link Capability} of the data to retrieve
     * @return {@link Payload} object identified by {@code id} or {@code null}.
     * @throws NullPointerException if {@code cap} is {@code null}.
     * @throws DataUnavailableException if {@code cap.getIdentifier()} does not exist.
     */
    QueryResult<Payload> getDataDirect(Capability cap) throws DataUnavailableException;

    /**
     * Returns data stored in the connected blockchain referenced by {@code id}.
     * Does not need an account but can only retrieve data that is not encrypted
     *
     * @param id {@link DataIdentifier} of the data to retrieve
     * @return {@link Payload} object identified by {@code id} or {@code null}.
     * @throws NullPointerException if {@code id} is {@code null}.
     * @throws DataUnavailableException if {@code id} does not exist.
     */
    QueryResult<Payload> getPublicData(DataIdentifier id) throws DataUnavailableException;


    /**
     * Persists data in the connected blockchain.
     * The data is unencrypted and everybody can read it.
     *  The data is passed as payload and as such links in it are not resolved while storing the data.
     *  If the payload contains links to other data over their capability the other data becomes available to everybody as well
     * @param payload {@link Payload} to persist
     * @return {@link DataIdentifier} of the persisted data, or
     * 			{@code null} if the data was invalid (@see Payload.isValid())
     * @throws NullPointerException if {@code payload} is {@code null}.
     */
    DataIdentifier publishData(Payload payload);

    /**
     * Encodes and returns the public key of a registered subject
     *  The result can be used to import a public key on another machine over {@code importSubject(pk)}
     * @param subject {@link Subject} to export
     * @return encoded public key of subject, or
     * 			{@code null} if the subject was not registered
     * @throws NullPointerException if {@code payload} is {@code null}.
     */
    String exportSubjectKey(Subject subject);


    /**
     * Decodes and impregisters a public key of a subject
     *  The input can be generated over {@code exportSubjectKey(subject)}
     * @param pk {@link String} to import
     * @return {@link Subject} that was registered
     * @throws TODO WHAT CAN COME HERE.
     */
    Subject importSubject(String pk);

    /**
     * Creates a new account owned by this node and authorized over password
     * @param password {@link String} to protect access to the account
     * @return {@link Subject} of the created account
     * 			{@code null} if {@code password} is {@code null}.
     */
    Subject createAccount(String password);

    //Todo: BackupFunctionality to export import accounts

    /**
     * Gets a service bound to account which transparently encrypts and decrypts the interactions with the data
     * @param account {@link Account} used to interact with the data
     * @return {@link PersonalCoreService} of the created account
     * @throws AuthenticationException if the account has an invalid password or a non-existing subject
     */
    PersonalCoreService getPersonalService(Account account) throws AuthenticationException;


    //todo: somwhere else???
    Map.Entry<Capability, byte[]> selfEncrypt(byte[] data);
    byte[] selfDecrypt(Capability key, byte[] data);


    /**
     * Adds a listener that is called whenever a new block is finalized
     * @param listener {@link Blockchain.BlockFinalizedListener} used as callback
     * @return {@code false} if the listener was already registered and true otherwise
     */
    boolean addUpdateListener(Blockchain.BlockFinalizedListener listener);

    /**
     * Removes a listener registered with {@code addUpdateListener(listener)}
     * @param listener {@link Blockchain.BlockFinalizedListener} to deregister
     * @return {@code false} if the listener was not registered and true otherwise
     */
    boolean removeUpdateListener(Blockchain.BlockFinalizedListener listener);

}
