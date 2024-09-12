package org.commonmark.renderer;

import org.commonmark.node.Node;

import java.util.Set;

/**
 * A renderer for a set of node types.
 */
public interface NodeRenderer {

    /**
     * @return the types of nodes that this renderer handles
     */
    Set<Class<? extends Node>> getNodeTypes();

    /**
     * Render the specified node.
     *
     * @param node the node to render, will be an instance of one of {@link #getNodeTypes()}
     */
    void render(Node node);

    /**
     * Called before the root node is rendered, to do any initial processing at the start.
     *
     * @param rootNode the root (top-level) node
     */
    default void beforeRoot(Node rootNode) {
    }

    /**
     * Called after the root node is rendered, to do any final processing at the end.
     *
     * @param rootNode the root (top-level) node
     */
    default void afterRoot(Node rootNode) {
    }
}
