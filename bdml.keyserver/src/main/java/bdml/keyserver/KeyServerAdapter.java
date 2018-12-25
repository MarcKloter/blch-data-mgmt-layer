package bdml.keyserver;

import bdml.keyserver.persistence.Subject;
import bdml.services.KeyServer;
import bdml.services.exceptions.MisconfigurationException;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.PublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class KeyServerAdapter implements KeyServer {
    private final String FILENAME = "registeredKeys.json";

    private Map<String, Subject> registeredKeys;

    public KeyServerAdapter() {
        // load previously generated key pairs
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(FILENAME);
        if(file.exists()) {
            try {
                    JsonParser jsonParser = new JsonFactory().createParser(file);
                    JavaType valueType = mapper.getTypeFactory().constructParametricType(Map.class, String.class, Subject.class);
                    this.registeredKeys = mapper.readValue(jsonParser, valueType);
            } catch (IOException e) {
                throw new MisconfigurationException(e.getMessage());
            }
        } else {
            this.registeredKeys = new HashMap<>();
        }
    }

    @Override
    public void registerKey(String identifier, PublicKey key) {
        String publicKey = Base64.getEncoder().encodeToString(key.getEncoded());
        this.registeredKeys.put(identifier, new Subject(identifier, publicKey, "test description"));

        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(new FileWriter(FILENAME, false), registeredKeys);
        } catch (IOException e) {
            throw new MisconfigurationException(e.getMessage());
        }
    }

    @Override
    public PublicKey queryKey(String identifier) {
        if(identifier == null)
            return null;

        Subject subject = registeredKeys.get(identifier);
        return (subject != null) ? KeyDecoder.decodePublicKey(subject.getPublicKey()) : null;
    }
}
