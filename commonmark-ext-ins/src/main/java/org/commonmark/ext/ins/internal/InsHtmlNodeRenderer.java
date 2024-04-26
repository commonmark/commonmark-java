package org.commonmark.ext.ins.internal;

import org.commonmark.node.Node;
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.renderer.html.HtmlWriter;

import java.util.Map;

public class InsHtmlNodeRenderer extends InsNodeRenderer {

    private final HtmlNodeRendererContext context;
    private final HtmlWriter html;

    public InsHtmlNodeRenderer(HtmlNodeRendererContext context) {
        this.context = context;
        this.html = context.getWriter();
    }

    @Override
    public void render(Node node) {
        Map<String, String> attributes = context.extendAttributes(node, "ins", Map.of());
        html.tag("ins", attributes);
        renderChildren(node);
        html.tag("/ins");
    }

    private void renderChildren(Node parent) {
        Node node = parent.getFirstChild();
        while (node != null) {
            Node next = node.getNext();
            context.render(node);
            node = next;
        }
    }
}
