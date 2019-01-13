package bdml.core.helper;

import bdml.services.exceptions.MisconfigurationException;

import java.io.*;
import java.util.Properties;

public class Configuration {
    /**
     * Loads the provided {@code defaultConfigFile} .properties file from the classpath.
     * Loads the provided {@code appConfigFileName} .properties file from the classpath if exists.
     * Loads the provided {@code appConfigFileName} .properties file from next to the JAR or the root of your java project if exists.
     *
     * The loaded properties are overwritten in the following order:
     * {@code appConfigFileName} from directory > {@code appConfigFileName} from classpath > {@code defaultConfigFile}
     *
     * @param appConfigFileName application .properties configuration file
     * @param defaultConfigFileName default fallback configuration file
     * @return Merged properties object of all three files.
     */
    public static Properties load(String appConfigFileName, String defaultConfigFileName) {
        Properties configuration = new Properties();

        InputStream defaultConfigFromResources = Configuration.class.getClassLoader().getResourceAsStream(defaultConfigFileName);
        configuration.putAll(loadPropertiesFile(defaultConfigFromResources));

        InputStream appConfigFromResources = Configuration.class.getClassLoader().getResourceAsStream(appConfigFileName);
        if(appConfigFromResources != null)
            configuration.putAll(loadPropertiesFile(appConfigFromResources));

        File appConfigFile = new File(appConfigFileName);
        if(appConfigFile.exists()) {
            try(InputStream appConfig = new FileInputStream(appConfigFile)) {
                configuration.putAll(loadPropertiesFile(appConfig));
            } catch (IOException e) {
                throw new MisconfigurationException(String.format("Error while loading the application configuration: %s", e.getMessage()));
            }
        }

        return configuration;
    }

    private static Properties loadPropertiesFile(InputStream inputStream) {
            Properties properties = new Properties();
            try {
                properties.load(inputStream);
            } catch (IOException e) {
                throw new MisconfigurationException(String.format("Error while loading the application configuration: %s", e.getMessage()));
            }
            return properties;

    }
}
