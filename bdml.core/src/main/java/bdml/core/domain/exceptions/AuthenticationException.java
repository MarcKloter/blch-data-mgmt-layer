package bdml.core.domain.exceptions;

public class AuthenticationException extends Exception {
    public  AuthenticationException() {
        super("Account password is invalid or account does not exist.");
    }
}
