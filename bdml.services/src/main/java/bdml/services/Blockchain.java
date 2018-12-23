package bdml.services;

public interface Blockchain {
    // TODO: javadoc

    /**
     * Creates an account (eg. pair of public and private key) to use within the connected blockchain.
     * An account can be used by providing the returned identifier together with the correct secret.
     *
     * @param secret secret to tie to the created account for future use
     * @return String representation of the identifier for the created account.
     */
    String createAccount(String secret);
}
