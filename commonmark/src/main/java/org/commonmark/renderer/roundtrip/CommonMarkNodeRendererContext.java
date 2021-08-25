package org.commonmark.renderer.roundtrip;

import org.commonmark.node.Node;

public interface CommonMarkNodeRendererContext {

    /**
     * @return true for stripping new lines and render text as "single line",
     * false for keeping all line breaks.
     */
    boolean stripNewlines();

    /**
     * @return the writer to use
     */
    CommonMarkWriter getWriter();

    /**
     * Render the specified node and its children using the configured renderers. This should be used to render child
     * nodes; be careful not to pass the node that is being rendered, that would result in an endless loop.
     *
     * @param node the node to render
     */
    void render(Node node);
}
