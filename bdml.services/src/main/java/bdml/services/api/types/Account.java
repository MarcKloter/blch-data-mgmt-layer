package bdml.services.api.types;

public class Account {
    private final String identifier;
    private final String password;

    public Account(String identifier, String password) {
        this.identifier = identifier;
        this.password = password;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getPassword() {
        return password;
    }
}
