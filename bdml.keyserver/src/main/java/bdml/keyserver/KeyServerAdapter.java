package bdml.keyserver;

import bdml.keyserver.persistence.Subject;
import bdml.services.KeyServer;
import bdml.services.exceptions.MisconfigurationException;
import bdml.services.exceptions.MissingConfigurationException;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PublicKey;
import java.util.*;

public class KeyServerAdapter implements KeyServer {
    // mandatory configuration properties
    private static final String OUTPUT_DIRECTORY_KEY = "bdml.output.directory";

    private static final String FILENAME = "keyPairMap.json";

    private final String FILEPATH;

    private Map<String, Subject> registeredKeys;

    public KeyServerAdapter(Properties configuration) {
        String outputDirectory = getProperty(configuration, OUTPUT_DIRECTORY_KEY);
        this.FILEPATH = outputDirectory + "/" + FILENAME;

        // load previously generated key pairs
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(FILEPATH);
        if(file.exists()) {
            try {
                    JsonParser jsonParser = new JsonFactory().createParser(file);
                    JavaType valueType = mapper.getTypeFactory().constructParametricType(Map.class, String.class, Subject.class);
                    this.registeredKeys = mapper.readValue(jsonParser, valueType);
            } catch (IOException e) {
                throw new MisconfigurationException(e.getMessage());
            }
        } else {
            // create path if it doesn't exist
            Path path = Paths.get(outputDirectory);
            if(Files.notExists(path)) {
                try {
                    Files.createDirectories(path);
                } catch (IOException e) {
                    throw new MisconfigurationException(e.getMessage());
                }
            }

            this.registeredKeys = new HashMap<>();
        }
    }

    private String getProperty(Properties configuration, String property) {
        if(!configuration.containsKey(property))
            throw new MissingConfigurationException(property);

        return configuration.getProperty(property);
    }

    @Override
    public void registerKey(String identifier, PublicKey key) {
        String publicKey = Base64.getEncoder().encodeToString(key.getEncoded());
        this.registeredKeys.put(identifier, new Subject(identifier, publicKey));

        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(new FileWriter(FILEPATH, false), registeredKeys);
        } catch (IOException e) {
            throw new MisconfigurationException(e.getMessage());
        }
    }

    @Override
    public PublicKey queryKey(String identifier) {
        if(identifier == null)
            return null;

        Subject subject = registeredKeys.get(identifier);

        return Optional.ofNullable(subject).map(s -> KeyDecoder.decodePublicKey(s.getPublicKey())).orElse(null);
    }
}
