package bdml.services;

import java.security.PublicKey;

public interface KeyServer {
    // TODO: javadoc

    void registerKey(String identifier, PublicKey key);

    /**
     * Returns the key associated with the given identifier or null.
     *
     * @param identifier
     * @return
     */
    PublicKey queryKey(String identifier);
}
