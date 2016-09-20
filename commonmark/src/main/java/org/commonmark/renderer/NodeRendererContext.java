package org.commonmark.renderer;

import org.commonmark.node.Node;

/**
 * The context for node rendering, including configuration and functionality for the node renderer to use.
 * <p><em>This interface is not intended to be implemented by clients.</em></p>
 */
public interface NodeRendererContext<W extends Writer> {

    /**
     * Render the specified node and its children using the configured renderers. This should be used to render child
     * nodes; be careful not to pass the node that is being rendered, that would result in an endless loop.
     *
     * @param node the node to render
     */
    void render(Node node);

    /**
     * @return the writer to use
     */
    W getWriter();
}
