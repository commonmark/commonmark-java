package org.commonmark.renderer.markdown;

import org.commonmark.node.Node;

import java.util.Set;

/**
 * Context that is passed to custom node renderers, see {@link MarkdownNodeRendererFactory#create}.
 */
public interface MarkdownNodeRendererContext {

    /**
     * @return the writer to use
     */
    MarkdownWriter getWriter();

    /**
     * Render the specified node and its children using the configured renderers. This should be used to render child
     * nodes; be careful not to pass the node that is being rendered, that would result in an endless loop.
     *
     * @param node the node to render
     */
    void render(Node node);

    /**
     * @return additional special characters that need to be escaped if they occur in normal text; currently only ASCII
     * characters are allowed
     */
    Set<Character> getSpecialCharacters();
}
