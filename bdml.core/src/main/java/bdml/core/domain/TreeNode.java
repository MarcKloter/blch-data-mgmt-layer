package bdml.core.domain;

import java.util.HashSet;
import java.util.Set;

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
