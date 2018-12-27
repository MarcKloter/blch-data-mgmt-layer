package bdml.services;

import bdml.services.api.types.Account;

public interface Cache {
    // TODO: javadoc

    void add(Account account, byte[] id, byte[] capability);

    byte[] get(Account account, byte[] id);
}
