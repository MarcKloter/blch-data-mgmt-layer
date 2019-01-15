package bdml.format;
import bdml.services.Formater;
import bdml.services.api.types.ParsedPayload;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayOutputStream;
import java.util.Properties;


public class FormaterImpl implements Formater {

    private final Kryo kryo;

    public FormaterImpl(Properties configuration) {
        this.kryo = new Kryo();
        this.kryo.setRegistrationRequired(false);

    }

    @Override
    public byte[] serialize(ParsedPayload data) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        Output output = new Output(b);
        kryo.writeClassAndObject(output, data);
        return output.toBytes();
    }

    @Override
    public ParsedPayload parse(byte[] payload) {
        Input input = new Input(payload);
        return(ParsedPayload)kryo.readClassAndObject(input);
    }
}