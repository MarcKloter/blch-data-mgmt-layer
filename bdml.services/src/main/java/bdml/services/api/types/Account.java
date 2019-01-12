package bdml.services.api.types;

import java.util.Objects;

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

    @Override
    public int hashCode() {
        return Objects.hash(identifier, password);
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) return true;
        if (!(other instanceof Account)) return false;
        final Account that = (Account) other;

        return Objects.equals(this.identifier, that.identifier) &&
               Objects.equals(this.password, that.password);
    }
}
