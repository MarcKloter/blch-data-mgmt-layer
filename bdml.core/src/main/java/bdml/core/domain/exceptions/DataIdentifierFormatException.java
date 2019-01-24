package bdml.core.domain.exceptions;

/**
 * Thrown to indicate that the application has attempted to interpret the input as a data identifier
 * but it did not have the appropriate format.
 */
public class DataIdentifierFormatException extends IllegalArgumentException {
    public DataIdentifierFormatException(String message) {
        super(message);
    }
}
