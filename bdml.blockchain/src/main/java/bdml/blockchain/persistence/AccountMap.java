package bdml.blockchain.persistence;

import bdml.services.exceptions.MisconfigurationException;
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
import java.util.HashMap;
import java.util.Map;

public class AccountMap {
    // TODO: load FILENAME from configuration file
    private final String DIRECTORY = "bdml-data";
    private final String FILENAME = "accountMap.json";
    private final String FILEPATH = DIRECTORY + "/" + FILENAME;

    private Map<String, String> accountMap;

    public AccountMap() {
        // load persisted accounts
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(FILEPATH);
        if(file.exists()) {
            try {
                JsonParser jsonParser = new JsonFactory().createParser(file);
                JavaType valueType = mapper.getTypeFactory().constructParametricType(Map.class, String.class, String.class);
                this.accountMap = mapper.readValue(jsonParser, valueType);
            } catch (IOException e) {
                throw new MisconfigurationException(e.getMessage());
            }
        } else {
            // create path if it doesn't exist
            Path path = Paths.get(DIRECTORY);
            if(Files.notExists(path)) {
                try {
                    Files.createDirectories(path);
                } catch (IOException e) {
                    throw new MisconfigurationException(e.getMessage());
                }
            }

            this.accountMap = new HashMap<>();
        }
    }

    public void put(String identifier, String address) {
        this.accountMap.put(identifier, address);

        // write map to file
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(new FileWriter(FILEPATH, false), accountMap);
        } catch (IOException e) {
            throw new MisconfigurationException(e.getMessage());
        }
    }

    public String get(String identifier) {
        return this.accountMap.get(identifier);
    }
}
