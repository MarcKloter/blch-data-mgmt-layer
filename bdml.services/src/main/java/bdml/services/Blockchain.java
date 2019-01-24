package bdml.services;

import bdml.services.helper.Account;
import bdml.services.helper.FrameListener;

import java.util.*;

public interface Blockchain {
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
     * Returns a pointer to the most recent block (eg. number or hash).
     *
     * @return Pointer of the current block the client is on.
     */
    String blockPointer();

    /**
     * Returns the frame previously stored using the given identifier.
     *
     * @param identifier unique identifier of previously stored data
     * @return Frame matching the given identifier or {@code null}.
     */
    byte[] getFrame(byte[] identifier);

    /**
     * Returns all frames stored subsequent to the {@code fromBlock}.
     * All frames stored in the {@code fromBlock} are excluded from the result.
     *
     * @apiNote LinkedHashSet is used to retain insertion order, which SortedSet would not support.
     *
     * @param fromBlock block pointer to start receiving frames after
     * @return Ordered set containing all identifier/frame pairs stored after the {@code fromBlock}
     * in chronological order (oldest first).
     */
    LinkedHashSet<Map.Entry<byte[], byte[]>> getFrames(String fromBlock);

    /**
     * Registers the given {@link FrameListener} for notifications about any new frames received from the connected blockchain.
     *
     * @param frameListener object that implements {@link FrameListener} to notify about new frames
     */
    void startFrameListener(FrameListener frameListener);

    /**
     * Unregisters any registered {@link FrameListener}.
     */
    void stopFrameListener();
}
