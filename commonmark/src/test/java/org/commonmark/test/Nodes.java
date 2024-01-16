package org.commonmark.test;

import org.commonmark.node.Node;

import java.util.ArrayList;
import java.util.List;

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
    public static <T> T find(Node parent, Class<T> nodeClass) {
        Node node = parent.getFirstChild();
        while (node != null) {
            Node next = node.getNext();
            if (nodeClass.isInstance(node)) {
                //noinspection unchecked
                return (T) node;
            }
            T result = find(node, nodeClass);
            if (result != null) {
                return result;
            }
            node = next;
        }
        return null;
    }
}
