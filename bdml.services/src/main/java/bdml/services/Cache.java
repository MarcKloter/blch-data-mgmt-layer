package bdml.services;

import bdml.services.api.types.Account;

public interface Cache {
    // TODO: javadoc

    void add(Account account, byte[] id, byte[] capability);

    /**
     * Queries the cache owned by the provided account for the provided identifier.
     *
     * @param account account to search the cache for
     * @param id byte array containing a 32 bytes identifier
     * @return Byte array containing the cached capability or null.
     */
    byte[] get(Account account, byte[] id);
}
