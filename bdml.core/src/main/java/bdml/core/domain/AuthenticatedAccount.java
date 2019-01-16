package bdml.core.domain;

import java.security.PublicKey;

/**
 * Extends {@link Account} by a public key field.
 */
public class AuthenticatedAccount extends Account {
    private PublicKey publicKey;

    public AuthenticatedAccount(Account account, PublicKey publicKey) {
        super(account.getSubject(), account.getPassword());
        this.publicKey = publicKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }
}
