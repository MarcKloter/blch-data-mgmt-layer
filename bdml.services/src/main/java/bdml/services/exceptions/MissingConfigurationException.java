package bdml.services.exceptions;

public class MissingConfigurationException extends RuntimeException {
    public MissingConfigurationException(String property) {
        super(String.format("The configuration '%s' is missing. Please specify it within the application.properties file.", property));
    }
}
