package org.commonmark.ext.gfm.strikethrough.internal;

import org.commonmark.ext.gfm.strikethrough.Strikethrough;
import org.commonmark.html.HtmlWriter;
import org.commonmark.html.renderer.NodeRenderer;
import org.commonmark.html.renderer.NodeRendererContext;
import org.commonmark.node.Node;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class StrikethroughNodeRenderer implements NodeRenderer {

    private final NodeRendererContext context;
    private final HtmlWriter html;

    public StrikethroughNodeRenderer(NodeRendererContext context) {
        this.context = context;
        this.html = context.getHtmlWriter();
    }

    @Override
    public Set<Class<? extends Node>> getNodeTypes() {
        return Collections.<Class<? extends Node>>singleton(Strikethrough.class);
    }

    @Override
    public void render(Node node) {
        Map<String, String> attributes = context.extendAttributes(node, Collections.<String, String>emptyMap());
        html.tag("del", attributes);
        renderChildren(node);
        html.tag("/del");
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
