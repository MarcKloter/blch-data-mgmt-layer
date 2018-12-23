package bdml.keyserver;

import bdml.keyserver.persistence.IdentifiablePublicKey;
import bdml.services.KeyServer;
import bdml.services.exceptions.MisconfigurationException;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

import static org.bouncycastle.util.encoders.Hex.toHexString;

public class KeyServerAdapter implements KeyServer {
    private final String FILENAME = "registeredKey.json";

    private List<IdentifiablePublicKey> registeredKeys;

    public KeyServerAdapter() {
        // load previously generated key pairs
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(FILENAME);
        if(file.exists()) {
            try {
                    JsonParser jsonParser = new JsonFactory().createParser(file);
                    CollectionType valueType = mapper.getTypeFactory().constructCollectionType(List.class, IdentifiablePublicKey.class);
                    this.registeredKeys = mapper.readValue(jsonParser, valueType);
            } catch (IOException e) {
                throw new MisconfigurationException(e.getMessage());
            }
        } else {
            this.registeredKeys = new ArrayList<>();
        }
    }

    @Override
    public void registerKey(String identifier, PublicKey key) {
        String publicKey = toHexString(key.getEncoded());
        this.registeredKeys.add(new IdentifiablePublicKey(identifier, publicKey));

        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(new FileWriter(FILENAME, false), registeredKeys);
        } catch (IOException e) {
            throw new MisconfigurationException(e.getMessage());
        }
    }
}
