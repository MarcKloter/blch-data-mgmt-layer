package bdml.services.api.exceptions;

public class AuthenticationException extends Exception {
    public  AuthenticationException() {
        super("Account password is invalid or account does not exist.");
    }
}
