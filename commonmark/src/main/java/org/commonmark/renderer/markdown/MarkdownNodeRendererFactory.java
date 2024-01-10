package org.commonmark.renderer.markdown;

import org.commonmark.renderer.NodeRenderer;

/**
 * Factory for instantiating new node renderers ƒor rendering.
 */
public interface MarkdownNodeRendererFactory {

    /**
     * Create a new node renderer for the specified rendering context.
     *
     * @param context the context for rendering (normally passed on to the node renderer)
     * @return a node renderer
     */
    NodeRenderer create(MarkdownNodeRendererContext context);
}