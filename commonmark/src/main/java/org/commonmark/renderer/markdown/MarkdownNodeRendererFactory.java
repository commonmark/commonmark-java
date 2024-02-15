package org.commonmark.renderer.markdown;

import org.commonmark.renderer.NodeRenderer;

import java.util.Set;

/**
 * Factory for instantiating new node renderers for rendering custom nodes.
 */
public interface MarkdownNodeRendererFactory {

    /**
     * Create a new node renderer for the specified rendering context.
     *
     * @param context the context for rendering (normally passed on to the node renderer)
     * @return a node renderer
     */
    NodeRenderer create(MarkdownNodeRendererContext context);

    /**
     * @return the additional special characters that this factory would like to have escaped in normal text; currently
     * only ASCII characters are allowed
     */
    Set<Character> getSpecialCharacters();
}
