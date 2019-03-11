package bdml.services;

import java.util.*;

public interface Blockchain {

    /**
     * Creates a transaction on the connected blockchain containing the given document.
     *
     * @param identifier unique identifier to store the data under
     * @param doc data to store in a transaction
     * @param encrypted tells if the capability is needed to read the document (or if the data identifier is enough)
     * @return false if the identity was already used and true otherwise.
     */
    boolean storeDocument(byte[] identifier, byte[] doc, boolean encrypted);

    /**
     * Creates a transaction on the connected blockchain containing the given access token.
     *
     * @param identifier which is indexed and used to query tokens
     * @param token data to store in a transaction
     */
    void storeAccessToken(byte[] identifier, byte[] token);

    /**
     * Creates a transaction on the connected blockchain containing the given amendment token.
     *
     * @param identifier which identifies the amended document
     * @param token data to store in a transaction
     */
    void storeAmendmentToken(byte[] identifier, byte[] token);


    /**
     * Returns a pointer to the most recent block (eg. number or hash).
     *
     * @return Pointer of the current block the client is on.
     */
    long blockPointer();

    /**
     * Returns the frame previously stored using the given identifier.
     *
     * @param identifier unique identifier of previously stored data
     * @param includePending is a marker that if true does look for non-final documents as well
     * @return Frames matching the given identifier (Can only be multiple if am attacker is around).
     */
    List<QueryResult<byte[]>> getDocument(byte[] identifier, boolean includePending);

    /**
     * Returns all access tokens that were published between {@code fromBlock} and {@code toBlock} with the identifier {@code identifier}.
     *
     * @param fromBlock block pointer to start receiving tokens after (inclusive)
     * @param toBlock block pointer to end receiving tokens before (exclusive)
     * @param identifier to filter tokens by
     * @return list containing all tokens stored between {@code fromBlock} and {@code toBlock} with the identifier {@code identifier}.
     * in chronological order (oldest first).
     */
     List<QueryResult<byte[]>> getAllTokens(long fromBlock, long toBlock, byte[] identifier);

    /**
     * Returns all amendment tokens that were published between {@code fromBlock} and {@code toBlock}.
     *
     * @param fromBlock block pointer to start receiving tokens after (inclusive)
     * @param toBlock block pointer to end receiving tokens before (exclusive)
     * @return list containing all tokens stored between {@code fromBlock} and {@code toBlock}.
     * in chronological order (oldest first).
     */
     List<QueryResult<Pair<byte[],byte[]>>> getAllAmendmentTokens(long fromBlock, long toBlock);

    /**
     * Returns all amendment tokens that were published for the document with id {@code identifier}.
     *
     * @param identifier to filter tokens by
     * @return list containing all amendments stored for the document with id {@code identifier}.
     * in chronological order (oldest first).
     */
     List<QueryResult<byte[]>> getAllAmendmentTokensFor(byte[] identifier);


    /**
     * Returns all unencrypted documents that were published between {@code fromBlock} and {@code toBlock}.
     *
     * @param fromBlock block pointer to start receiving tokens after (inclusive)
     * @param toBlock block pointer to end receiving tokens before (exclusive)
     * @return list containing all tokens stored between {@code fromBlock} and {@code toBlock}.
     * in chronological order (oldest first).
     */
     List<QueryResult<byte[]>> getAllPlainIds(long  fromBlock, long toBlock);

    /**
     * Listener interface to get updated when a new block with new data arrives
     */
     @FunctionalInterface
     interface BlockFinalizedListener {
         void newBlockFinalized(long blockNo);
     }
    /**
     * Adds a listener that is called whenever a new block is finalized
     * @param listener {@link Blockchain.BlockFinalizedListener} used as callback
     * @return {@code false} if the listener was already registered and true otherwise
     */
    boolean addBlockListener(BlockFinalizedListener listener);

    /**
     * Removes a listener registered with {@code addBlockListener(listener)}
     * @param listener {@link Blockchain.BlockFinalizedListener} to deregister
     * @return {@code false} if the listener was not registered and true otherwise
     */
     boolean removeBlockListener(BlockFinalizedListener listener);
}
