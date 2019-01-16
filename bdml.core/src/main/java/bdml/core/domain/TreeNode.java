package bdml.core.domain;

import bdml.core.jsonrpc.serializer.TreeNodeSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.HashSet;
import java.util.Set;

@JsonSerialize(using = TreeNodeSerializer.class)
public class TreeNode<T> {
    private T value;
    private Set<TreeNode<T>> children;

    public TreeNode(T value, Set<TreeNode<T>> children) {
        this.value = value;
        this.children = children;
    }

    public TreeNode(T value) {
        this.value = value;
        this.children = new HashSet<>();
    }

    public T getValue() {
        return value;
    }

    public Set<TreeNode<T>> getChildren() {
        return children;
    }
}
