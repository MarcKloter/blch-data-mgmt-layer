package bdml.core.domain;

import java.util.Objects;

public class Account implements bdml.services.helper.Account {
    private final Subject subject;
    private final String password;

    public Account(Subject subject, String password) {
        if(subject == null)
            throw new IllegalArgumentException("The subject cannot be null.");

        if(password == null)
            throw new IllegalArgumentException("The password cannot be null.");

        this.subject = subject;
        this.password = password;
    }

    public Subject getSubject() {
        return subject;
    }

    @Override
    public String getIdentifier() {
        return subject.toString();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIdentifier(), password);
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) return true;
        if (!(other instanceof Account)) return false;
        final Account that = (Account) other;

        return Objects.equals(this.getIdentifier(), that.getIdentifier()) &&
               Objects.equals(this.password, that.password);
    }
}
