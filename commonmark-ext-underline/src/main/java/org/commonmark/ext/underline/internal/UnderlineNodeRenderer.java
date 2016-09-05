package org.commonmark.ext.underline.internal;

import org.commonmark.ext.underline.Underline;
import org.commonmark.html.HtmlWriter;
import org.commonmark.html.renderer.NodeRenderer;
import org.commonmark.html.renderer.NodeRendererContext;
import org.commonmark.node.Node;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class UnderlineNodeRenderer implements NodeRenderer {

    private final NodeRendererContext context;
    private final HtmlWriter html;

    public UnderlineNodeRenderer(NodeRendererContext context) {
        this.context = context;
        this.html = context.getHtmlWriter();
    }

    @Override
    public Set<Class<? extends Node>> getNodeTypes() {
        return Collections.<Class<? extends Node>>singleton(Underline.class);
    }

    @Override
    public void render(Node node) {
        Map<String, String> attributes = context.extendAttributes(node, Collections.<String, String>emptyMap());
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
