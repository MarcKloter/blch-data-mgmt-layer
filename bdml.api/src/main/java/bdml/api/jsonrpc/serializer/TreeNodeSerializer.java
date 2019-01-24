package bdml.api.jsonrpc.serializer;

import bdml.core.domain.DataIdentifier;
import bdml.core.domain.TreeNode;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class TreeNodeSerializer extends StdSerializer<TreeNode<DataIdentifier>> {
    public TreeNodeSerializer() {
        this(null);
    }

    public  TreeNodeSerializer(Class<TreeNode<DataIdentifier>> t) {
        super(t);
    }

    @Override
    public void serialize(TreeNode<DataIdentifier> treeNode, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        writeTreeNode(treeNode, jsonGenerator);
    }

    private void writeTreeNode(TreeNode<DataIdentifier> treeNode, JsonGenerator jsonGenerator) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("identifier", treeNode.getValue().toString());
        jsonGenerator.writeFieldName("attachments");
        jsonGenerator.writeStartArray();
        for(TreeNode<DataIdentifier> child : treeNode.getChildren()) {
            writeTreeNode(child, jsonGenerator);
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeEndObject();
    }
}
