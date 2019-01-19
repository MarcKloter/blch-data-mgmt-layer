package bdml.services;

import java.security.PublicKey;

public interface KeyServer {
    /**
     * Registers the given {@link PublicKey} at the connected key server.
     *
     * @param identifier String to identify the key for retrieval
     * @param key public key to register
     * @throws IllegalArgumentException if the given {@code identifier} has been registered before.
     * @throws NullPointerException if {@code identifier} or {@code key} is {@code null}.
     */
    void registerKey(String identifier, PublicKey key);

    /**
     * Returns the key associated with the given identifier or null.
     *
     * @param identifier String that identifies a public key stored in the connected key server
     * @return {@link PublicKey} associated with the given {@code identifier} or {@code null}.
     */
    PublicKey queryKey(String identifier);
}
