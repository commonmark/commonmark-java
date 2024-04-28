package org.commonmark.test;

import org.commonmark.node.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Nodes {

    public static List<Node> getChildren(Node parent) {
        List<Node> children = new ArrayList<>();
        for (Node child = parent.getFirstChild(); child != null; child = child.getNext()) {
            children.add(child);
        }
        return children;
    }

    /**
     * Recursively try to find a node with the given type within the children of the specified node.
     *
     * @param parent    The node to get children from (node itself will not be checked)
     * @param nodeClass The type of node to find
     */
    public static <T> T tryFind(Node parent, Class<T> nodeClass) {
        Node node = parent.getFirstChild();
        while (node != null) {
            Node next = node.getNext();
            if (nodeClass.isInstance(node)) {
                //noinspection unchecked
                return (T) node;
            }
            T result = tryFind(node, nodeClass);
            if (result != null) {
                return result;
            }
            node = next;
        }
        return null;
    }

    /**
     * Recursively try to find a node with the given type within the children of the specified node. Throw if node
     * could not be found.
     */
    public static <T> T find(Node parent, Class<T> nodeClass) {
        return Objects.requireNonNull(tryFind(parent, nodeClass),
                "Could not find a " + nodeClass.getSimpleName() + " node in " + parent);
    }
}
