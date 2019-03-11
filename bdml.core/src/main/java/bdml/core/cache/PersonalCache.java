package bdml.core.cache;

import bdml.core.domain.Capability;
import bdml.core.domain.DataIdentifier;
import bdml.core.domain.TreeNode;
import bdml.services.helper.Account;

import java.util.Optional;
import java.util.Set;

public interface PersonalCache {

    /**
     * @param capability
     */
    void addCapability(Capability capability, boolean temporary);

    void addLink(DataIdentifier source, DataIdentifier target, boolean isAmend);

    /**
     * Queries the cache owned by the provided account for the provided identifier.
     *
     * @param identifier byte array containing a 32 bytes identifier
     * @return Byte array containing the cached capability or null.
     */
    Optional<Capability> getCapability(DataIdentifier identifier);

    Set<DataIdentifier> getLink(DataIdentifier identifier, boolean isAmend);

    //Returns the status of isTemporary before update
    Optional<Boolean> makePermanentIfExists(DataIdentifier identifier);

    Set<DataIdentifier> getAllIdentifiers();

    Set<DataIdentifier> getNewIdentifiers();


    long getPointer();

    void setPointer(long pointer);

}
