package bdml.core.domain.exceptions;

public class NotAuthorizedException extends RuntimeException {
    public  NotAuthorizedException(String message) {
        super(message);
    }
}
