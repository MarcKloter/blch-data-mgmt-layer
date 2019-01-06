package bdml.services;

import bdml.services.api.types.Account;

import java.util.Collection;
import java.util.List;

public interface Blockchain {
    // TODO: javadoc

    /**
     * Creates an entity (eg. pair of public and private key) to use within the connected blockchain.
     * An entity can be used by providing the id and secret used for creation.
     * An unique internal identifier (eg. address) for the created entity is returned for the caller to prepare it for use (eg. transfer coins).
     *
     * @param account account to tie to the entity for future use
     * @return Blockchain internal identifier  of the created entitiy.
     */
    String createEntity(Account account);

    /**
     * Creates a transaction on the connected blockchain containing the given frame.
     *
     * @param account    account tied to the entity to use
     * @param identifier identifier to store the data under
     * @param frame      data to store in a transaction
     */
    void storeFrame(Account account, byte[] identifier, byte[] frame);

    /**
     * Returns all frames previously stored using the given identifier.
     *
     * @param identifier identifier of previously stored data
     * @return Collection containing the requested frames, might be empty.
     */
    Collection<byte[]> getFrames(byte[] identifier);
}
