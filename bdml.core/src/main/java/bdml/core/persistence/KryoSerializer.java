package bdml.core.persistence;


import bdml.core.domain.Capability;
import bdml.core.domain.DataIdentifier;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.DefaultSerializers;

import java.io.ByteArrayOutputStream;

public class KryoSerializer implements Serializer {

    private static final Kryo kryo;

    static {
        kryo = new Kryo();
        kryo.setRegistrationRequired(false);
        kryo.register(Capability.class, new com.esotericsoftware.kryo.Serializer<Capability>(){
            @Override
            public void write(Kryo kryo, Output output, Capability o) {
                output.write(o.toByteArray());
            }

            @Override
            public Capability read(Kryo kryo, Input input, Class aClass) {
                return new Capability(input.readBytes(Capability.BYTES));
            }
        },0);
        kryo.register(DataIdentifier.class, new com.esotericsoftware.kryo.Serializer<DataIdentifier>(){
            @Override
            public void write(Kryo kryo, Output output, DataIdentifier o) {
                output.write(o.toByteArray());
            }

            @Override
            public DataIdentifier read(Kryo kryo, Input input, Class aClass) {
                return new DataIdentifier(input.readBytes(Capability.BYTES));
            }
        },1);
        kryo.register(String.class,new DefaultSerializers.StringSerializer(),2);
        kryo.register(Frame.class,3);
    }

    @Override
    public byte[] serializePayload(Payload payload) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        Output output = new Output(b);
        kryo.writeClassAndObject(output, payload);
        return output.toBytes();
    }

    @Override
    public Payload deserializePayload(byte[] payload) throws DeserializationException {
        try {
            Input input = new Input(payload);
            return (Payload)kryo.readClassAndObject(input);
        } catch (Exception e){
            throw new DeserializationException(e.getMessage());
        }

    }

    @Override
    public byte[] serializeDocument(Document doc) {
        //Make sure that we do not serialize a subclass
        Document realDoc = new Document(doc.getVersion(),doc.getPayload());
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        Output output = new Output(b);
        kryo.writeObject(output, realDoc);
        return output.toBytes();
    }

    @Override
    public Document deserializeDocument(byte[] doc) throws DeserializationException {
        try {
            Input input = new Input(doc);
            return kryo.readObject(input, Document.class);
        } catch (Exception e){
            throw new DeserializationException(e.getMessage());
        }
    }
}
