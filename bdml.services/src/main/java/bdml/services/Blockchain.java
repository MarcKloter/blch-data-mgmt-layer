package bdml.services;

import bdml.services.api.types.Account;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Blockchain {
    // TODO: javadoc

    /**
     * Creates an entity (eg. pair of public and private key) to use within the connected blockchain.
     * An entity can be used by providing the id and secret used for creation.
     * An unique internal identifier (eg. address) for the created entity is returned for the caller
     * to prepare it for use (eg. transfer coins).
     *
     * @param account account to tie to the entity for future use
     * @return Blockchain internal identifier  of the created entity.
     */
    String createEntity(Account account);

    /**
     * Creates a transaction on the connected blockchain containing the given frame.
     *
     * @param account    account tied to the entity to use
     * @param identifier unique identifier to store the data under
     * @param frame      data to store in a transaction
     */
    void storeFrame(Account account, byte[] identifier, byte[] frame);

    /**
     * Returns the identifier of the most recently added frame.
     *
     * @return Last added identifier or null if there are no frames.
     */
    byte[] getLatestIdentifier();

    /**
     * Returns the frame previously stored using the given identifier.
     *
     * @param identifier unique identifier of previously stored data
     * @return Frame matching the given identifier or {@code null}.
     */
    byte[] getFrame(byte[] identifier);

    /**
     * Returns all frames stored subsequent to the frame identified by the given {@code fromIdentifier}.
     * All frames stored in the block of {@code fromIdentifier} are excluded from the result.
     * Passing {@code null} for {@code fromIdentifier} will default to the earliest frame (inclusive).
     *
     * @param fromIdentifier unique identifier of previously stored data
     * @return Ordered list containing all identifier and frame pairs stored after the block of {@code fromIdentifier}
     * in chronological order (oldest first) or {@code null} if {@code fromIdentifier} does not exist.
     */
    Set<Map.Entry<byte[], byte[]>> getFrames(byte[] fromIdentifier);

}
