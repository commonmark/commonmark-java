package org.commonmark.html.renderer;

import org.commonmark.html.HtmlWriter;
import org.commonmark.node.Node;
import org.commonmark.renderer.BaseNodeRendererContext;

import java.util.Map;

public abstract class HtmlNodeRendererContext extends BaseNodeRendererContext<HtmlWriter> {

    /**
     * @param url to be encoded
     * @return an encoded URL (depending on the configuration)
     */
    public abstract String encodeUrl(String url);

    /**
     * Extend the attributes by extensions.
     *
     * @param node       the node for which the attributes are applied
     * @param attributes the attributes that were calculated by the renderer
     * @return the extended attributes with added/updated/removed entries
     */
    public abstract Map<String, String> extendAttributes(Node node, Map<String, String> attributes);

    /**
     * @return HTML that should be rendered for a soft line break
     */
    public abstract String getSoftbreak();

    /**
     * @return whether HTML blocks and tags should be escaped or not
     */
    public abstract boolean shouldEscapeHtml();
}
