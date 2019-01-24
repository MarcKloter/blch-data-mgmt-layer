package bdml.core.domain.exceptions;

/**
 * Thrown to indicate that the application has attempted to interpret the input as a capability
 * but it did not have the appropriate format.
 */
public class CapabilityFormatException extends IllegalArgumentException {
    public CapabilityFormatException(String message) {
        super(message);
    }
}
