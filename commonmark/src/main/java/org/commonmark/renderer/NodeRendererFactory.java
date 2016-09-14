package org.commonmark.renderer;

/**
 * Factory for instantiating new node renderers when rendering is done.
 */
public interface NodeRendererFactory<C extends NodeRendererContext> {

    /**
     * Create a new node renderer for the specified rendering context.
     *
     * @param context the context for rendering (normally passed on to the node renderer)
     * @return a node renderer
     */
    NodeRenderer create(C context);
}
