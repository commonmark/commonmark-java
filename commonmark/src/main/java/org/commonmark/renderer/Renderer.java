package org.commonmark.renderer;

import org.commonmark.node.Node;

public interface Renderer<Output, Render> {

    /**
     * Render the tree of nodes to output.
     *
     * @param node the root node
     * @param output output for rendering
     */
    void render(Node node, Output output);

    /**
     * Render the tree of nodes to string.
     *
     * @param node the root node
     * @return the rendered render
     */
    Render render(Node node);
}
