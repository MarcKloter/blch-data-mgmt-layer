package bdml.core.helper;

import bdml.services.api.types.Account;

import java.security.PublicKey;

/**
 * Adds a public key field to the API Account.
 */
public class AuthenticatedAccount extends Account {
    private PublicKey publicKey;

    public AuthenticatedAccount(Account account, PublicKey publicKey) {
        super(account.getIdentifier(), account.getPassword());
        this.publicKey = publicKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }
}
