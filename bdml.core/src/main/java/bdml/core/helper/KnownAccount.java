package bdml.core.helper;

import bdml.services.api.types.Account;

import java.security.PublicKey;

/**
 * Adds a public key field to the API Account.
 */
public class KnownAccount extends Account {
    private PublicKey publicKey;

    public KnownAccount(Account account, PublicKey publicKey) {
        super(account.getIdentifier(), account.getPassword());
        this.publicKey = publicKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }
}
