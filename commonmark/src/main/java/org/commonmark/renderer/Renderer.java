package org.commonmark.renderer;

import org.commonmark.node.Node;

public interface Renderer<T> {

    /**
     * Render the tree of nodes to output.
     *
     * @param node the root node
     * @param output output for rendering
     */
    void render(Node node, Appendable output);

    /**
     * Render the tree of nodes to string.
     *
     * @param node the root node
     * @return the rendered result
     */
    T render(Node node);
}
