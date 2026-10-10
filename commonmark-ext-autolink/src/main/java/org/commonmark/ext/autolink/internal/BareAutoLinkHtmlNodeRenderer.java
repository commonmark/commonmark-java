package org.commonmark.ext.autolink.internal;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.commonmark.ext.autolink.BareAutoLink;
import org.commonmark.node.Node;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.renderer.html.HtmlWriter;

public class BareAutoLinkHtmlNodeRenderer implements NodeRenderer {

    private final HtmlNodeRendererContext context;
    private final HtmlWriter html;

    public BareAutoLinkHtmlNodeRenderer(HtmlNodeRendererContext context) {
        this.context = context;
        this.html = context.getWriter();
    }

    @Override
    public Set<Class<? extends Node>> getNodeTypes() {
        return Set.of(BareAutoLink.class);
    }

    @Override
    public void render(Node node) {
        BareAutoLink autoLink = (BareAutoLink) node;
        Map<String, String> attrs = new LinkedHashMap<>();
        String url = autoLink.getDestination();
        if (context.shouldSanitizeUrls()) {
            url = context.urlSanitizer().sanitizeLinkUrl(url);
            attrs.put("rel", "nofollow");
        }
        attrs.put("href", context.encodeUrl(url));
        html.tag("a", context.extendAttributes(node, "a", attrs));
        html.text(autoLink.getContent());
        html.tag("/a");
    }
}
