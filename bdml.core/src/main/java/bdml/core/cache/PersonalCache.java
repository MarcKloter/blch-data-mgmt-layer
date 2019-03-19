package bdml.core.cache;

import bdml.core.domain.Capability;
import bdml.core.domain.DataIdentifier;
import bdml.core.domain.TreeNode;
import bdml.services.helper.Account;

import java.util.Optional;
import java.util.Set;

public interface PersonalCache {


    enum Status {
        Missing, Temporary, Permanent
    }

    /**
     * @param capability
     * @return the status of the capability before it was added
     */
    Status addCapability(Capability capability, boolean temporary);

    Status addLink(DataIdentifier source, DataIdentifier target, boolean isAmend, boolean isTemporary);

    /**
     * Queries the cache owned by the provided account for the provided identifier.
     *
     * @param identifier byte array containing a 32 bytes identifier
     * @return Byte array containing the cached capability or null.
     */
    Optional<Capability> getCapability(DataIdentifier identifier, boolean includeTemporary);

    Set<DataIdentifier> getLinkTarget(DataIdentifier identifier, boolean isAmend, boolean includeTemporary);

    //Returns the status of isTemporary before update
    Status makeDataPermanentIfExists(DataIdentifier identifier);

    Set<DataIdentifier> getAllIdentifiers();

    Set<DataIdentifier> getNewIdentifiers();


    long getPointer();

    void setPointer(long pointer);

}
