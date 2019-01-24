package bdml.api.jsonrpc.types;

import bdml.api.jsonrpc.serializer.TreeNodeSerializer;
import bdml.core.domain.DataIdentifier;
import bdml.core.domain.TreeNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class ListAttachmentsResponse {
    @JsonSerialize(using = TreeNodeSerializer.class)
    private TreeNode<DataIdentifier> result;

    public ListAttachmentsResponse(TreeNode<DataIdentifier> result) {
        this.result = result;
    }
}
