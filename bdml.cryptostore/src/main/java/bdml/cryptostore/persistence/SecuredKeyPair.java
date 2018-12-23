package bdml.cryptostore.persistence;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SecuredKeyPair {
    private String publicKey;
    private String privateKey;
    private String passwordHash;

    @JsonCreator
    public SecuredKeyPair(@JsonProperty("publicKey") String publicKey,
                          @JsonProperty("privateKey") String privateKey,
                          @JsonProperty("passwordHash") String passwordHash) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.passwordHash = passwordHash;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}
