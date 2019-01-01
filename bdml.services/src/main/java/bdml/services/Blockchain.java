package bdml.services;

import bdml.services.api.types.Account;

public interface Blockchain {
    // TODO: javadoc

    /**
     * Creates an entity (eg. pair of public and private key) to use within the connected blockchain.
     * An entity can be used by providing the id and secret used for creation.
     *
     * @param account account to tie to the entity for future use
     */
    void createEntity(Account account);

    /**
     * Creates a transaction containing the given payload on the connected blockchain.
     *
     * @param account account tied to the entity to use
     * @param identifier identifier to store the data under
     * @param payload data to store in a transaction
     */
    void createTransaction(Account account, byte[] identifier, byte[] payload);

    /**
     * Returns previously stored data by an identifier.
     *
     * @param identifier unique identifier of previously stored data
     * @return The requested data or null if none was found.
     */
    byte[] getTransaction(byte[] identifier);
}
