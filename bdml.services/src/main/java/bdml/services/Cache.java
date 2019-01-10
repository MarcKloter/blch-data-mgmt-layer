package bdml.services;

import bdml.services.api.types.Account;
import bdml.services.api.types.Identifier;

import java.util.Set;

public interface Cache {
    // TODO: javadoc

    /**
     * Creates a new cache for the given account at the provided pointer.
     *
     * @param account account to create the cache for
     * @param pointer latest 32 bytes identifier to initialize the cache pointer
     * @throws IllegalStateException if {@code account} was already initialized.
     */
    void initialize(Account account, byte[] pointer);

    /**
     *
     * @param account
     * @param identifier
     * @param capability
     */
    void addCapability(Account account, byte[] identifier, byte[] capability, boolean isAttachment);

    /**
     * Queries the cache owned by the provided account for the provided identifier.
     *
     * @param account account to search the cache for
     * @param identifier byte array containing a 32 bytes identifier
     * @return Byte array containing the cached capability or null.
     */
    byte[] getCapability(Account account, byte[] identifier);

    void setRecursivelyParsed(Account account, byte[] identifier);

    boolean wasRecursivelyParsed(Account account, byte[] identifier);

    Set<byte[]> getDirectlyAccessibleIdentifiers(Account account);

    byte[] getPointer(Account account);

    void movePointer(Account account, byte[] identifier);

    void markAsParsed(Account account, byte[] identifier);

    boolean wasParsedBefore(Account account, byte[] identifier);

    void addAttachment(Account account, byte[] identifier, byte[] attachedTo);

    Identifier getAllAttachments(Account account, byte[] identifier);
}
