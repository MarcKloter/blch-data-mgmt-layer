package bdml.core.cache;

import bdml.core.domain.Capability;
import bdml.core.domain.DataIdentifier;
import bdml.core.domain.TreeNode;
import bdml.services.helper.Account;

import java.util.Optional;
import java.util.Set;

public interface Cache {
    // TODO: javadoc

    /**
     * Creates a new cache for the given account at the provided pointer.
     *
     * @param account account to create the cache for
     * @param pointer block number to initialize the cache pointer at
     * @throws IllegalStateException if {@code account} was already initialized.
     */
    void initialize(Account account, String pointer);

    /**
     *
     * @param account
     * @param capability
     */
    void addCapability(Account account, Capability capability, boolean isAttachment);

    /**
     * Queries the cache owned by the provided account for the provided identifier.
     *
     * @param account account to search the cache for
     * @param identifier byte array containing a 32 bytes identifier
     * @return Byte array containing the cached capability or null.
     */
    Optional<Capability> getCapability(Account account, DataIdentifier identifier);

    Set<DataIdentifier> getAllIdentifiers(Account account, boolean includeAttachments);

    void setRecursivelyParsed(Account account, DataIdentifier identifier);

    boolean wasRecursivelyParsed(Account account, DataIdentifier identifier);

    String getPointer(Account account);

    void setPointer(Account account, String pointer);

    String getPollPointer(Account account);

    void setPollPointer(Account account, String pointer);

    void addAttachment(Account account, DataIdentifier attachment, DataIdentifier attachedTo);

    TreeNode<DataIdentifier> getAllAttachments(Account account, DataIdentifier identifier);
}
