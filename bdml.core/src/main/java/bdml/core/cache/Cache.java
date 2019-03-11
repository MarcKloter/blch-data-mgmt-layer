package bdml.core.cache;

import bdml.core.domain.Capability;
import bdml.core.domain.DataIdentifier;
import bdml.core.domain.TreeNode;
import bdml.services.helper.Account;

import java.util.Optional;
import java.util.Set;

public interface Cache {
    /**
     * Creates a new cache for the given account at the provided pointer.
     *
     * @param account {@link Account} to create the cache for
     * @param pointer block number to initialize the cache pointer at
     * @throws IllegalStateException if {@code account} was already initialized.
     */
    void initialize(Account account, String pointer);

    /**
     * Stores the provided capability in the cache of the given account.
     *
     * @param account {@link Account} to cache the capability for
     * @param capability {@link Capability} to store
     * @param isAttachment marker whether the capability was obtained as an attachment
     * @throws IllegalArgumentException if the given {@code capability} is malformed
     */
    void addCapability(Account account, Capability capability, boolean isAttachment);

    /**
     * Queries the cache owned by the provided account for the provided identifier.
     *
     * @param account {@link Account} to search the cache for
     * @param identifier byte array containing a 32 bytes identifier
     * @return Byte array containing the cached capability or null.
     */
    Optional<Capability> getCapability(Account account, DataIdentifier identifier);

    /**
     * Returns the identifiers of all capability previously cached by the given account.
     *
     * @param account {@link Account} to search the cache for
     * @param includeAttachments flag whether attachments should be added to the returned set
     * @return {@link DataIdentifier} set of all cached capabilities.
     */
    Set<DataIdentifier> getAllIdentifiers(Account account, boolean includeAttachments);

    /**
     * Marks the capability identified by {@code identifier} as recursively parsed, which indicates that all of its
     * attachments as well as all of their successors have been parsed and cached. Thus, future operations on the frame
     * corresponding to {@code identifier} will access the cache instead of parsing attachments.
     *
     * @param account {@link Account} to interact as
     * @param identifier {@link DataIdentifier} corresponding to a previously cached capability
     * @throws IllegalArgumentException if the given {@code identifier} does not correspond to a cached capability
     */
    void setRecursivelyParsed(Account account, DataIdentifier identifier);

    /**
     * Returns the recursivelyParsed flag described in {@link Cache#setRecursivelyParsed(Account, DataIdentifier)}.
     *
     * @param account {@link Account} to interact as
     * @param identifier {@link DataIdentifier} corresponding to a previously cached capability
     * @return Boolean representing the recursivelyParsed flag.
     */
    boolean wasRecursivelyParsed(Account account, DataIdentifier identifier);

    /**
     * Returns the pointer to the block the cache of the given {@code account} currently is at.
     * This is, up to which block the cache contains frames.
     *
     * @param account {@link Account} to interact as
     * @return String previously set by {@link Cache#setPointer(Account, String)}.
     */
    String getPointer(Account account);

    /**
     * Sets the given String as pointer to the block the cache of the given {@code account} currently is at.
     *
     * @param account {@link Account} to interact as
     * @param pointer String identifying a block, such as the block number or hash.
     */
    void setPointer(Account account, String pointer);

    /**
     * Returns the pointer to the block at which the given {@code account} has polled frames the last time.
     * This pointer is different from {@link Cache#getPointer(Account)}, as the cache can be extended (through
     * {@link Cache#addCapability(Account, Capability, boolean)} without calling a polling method.
     *
     * @param account {@link Account} to interact as
     * @return String previously set by {@link Cache#setPollPointer(Account, String)}.
     */
    String getPollPointer(Account account);

    /**
     * Sets the pointer to the block the given {@code account} has called a polling function last.
     *
     * @param account {@link Account} to interact as
     * @param pointer String identifying a block, such as the block number or hash.
     */
    void setPollPointer(Account account, String pointer);

    /**
     * Adds the frame identified by the given {@link DataIdentifier} as attachment to the cache.
     * For an attachment, the cache holds its parent to form the tree of attachments on demand.
     *
     * @param account {@link Account} to interact as
     * @param attachment {@link DataIdentifier} corresponding to a previously cached capability
     * @param attachedTo {@link DataIdentifier} corresponding to the frame that the attachment was obtained from
     * @throws IllegalArgumentException if the given {@code attachment} or {@code attachedTo} is malformed
     */
    void addAttachment(Account account, DataIdentifier attachment, DataIdentifier attachedTo);

    /**
     * Returns a {@link TreeNode} representing the frame identified by the given {@code identifier}.
     * This node can be used to traverse through the tree of attachments associated to (= accessible through)
     * {@code identifier}.
     *
     * @param account {@link Account} to interact as
     * @param identifier {@link DataIdentifier} corresponding to a previously cached capability
     * @return {@link TreeNode} of the given {@code identifier}.
     */
    TreeNode<DataIdentifier> getAllAttachments(Account account, DataIdentifier identifier);
}
