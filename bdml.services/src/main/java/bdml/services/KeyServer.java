package bdml.services;

import java.security.PublicKey;

public interface KeyServer {
    // TODO: javadoc

    void registerKey(String identifier, PublicKey key);
}
