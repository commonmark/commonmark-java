package org.commonmark.html.renderer;

import org.commonmark.html.HtmlWriter;
import org.commonmark.node.Node;

import java.util.Map;

/**
 * The context for node rendering, including configuration and functionality for the node renderer to use.
 */
public interface NodeRendererContext {

    /**
     * @param url to be encoded
     * @return an encoded URL (depending on the configuration)
     */
    String encodeUrl(String url);

    /**
     * Extend the attributes by extensions.
     *
     * @param node       the node for which the attributes are applied
     * @param attributes the attributes that were calculated by the renderer
     * @return the extended attributes with added/updated/removed entries
     */
    Map<String, String> extendAttributes(Node node, Map<String, String> attributes);

    /**
     * @return the HTML writer to use
     */
    HtmlWriter getHtmlWriter();

    /**
     * @return HTML that should be rendered for a soft line break
     */
    String getSoftbreak();

    /**
     * Render the specified node and its children using the configured renderers. This should be used to render child
     * nodes; be careful not to pass the node that is being rendered, that would result in an endless loop.
     *
     * @param node the node to render
     */
    void render(Node node);

    /**
     * @return whether HTML blocks and tags should be escaped or not
     */
    boolean shouldEscapeHtml();
}
