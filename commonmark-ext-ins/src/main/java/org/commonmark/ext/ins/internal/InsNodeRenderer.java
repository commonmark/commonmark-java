package org.commonmark.ext.ins.internal;

import org.commonmark.ext.ins.Ins;
import org.commonmark.renderer.html.HtmlWriter;
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.node.Node;
import org.commonmark.renderer.NodeRenderer;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class InsNodeRenderer implements NodeRenderer {

    private final HtmlNodeRendererContext context;
    private final HtmlWriter html;

    public InsNodeRenderer(HtmlNodeRendererContext context) {
        this.context = context;
        this.html = context.getWriter();
    }

    @Override
    public Set<Class<? extends Node>> getNodeTypes() {
        return Collections.<Class<? extends Node>>singleton(Ins.class);
    }

    @Override
    public void render(Node node) {
        Map<String, String> attributes = context.extendAttributes(node, "ins", Collections.<String, String>emptyMap());
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
