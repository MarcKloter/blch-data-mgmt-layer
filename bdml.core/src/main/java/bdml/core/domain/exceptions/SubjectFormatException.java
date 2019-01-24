package bdml.core.domain.exceptions;

/**
 * Thrown to indicate that the application has attempted to interpret the input as a subject
 * but it did not have the appropriate format.
 */
public class SubjectFormatException extends IllegalArgumentException {
    public SubjectFormatException(String message) {
        super(message);
    }
}