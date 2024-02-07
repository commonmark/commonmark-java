package org.commonmark.ext.ins.internal;

import org.commonmark.node.Node;
import org.commonmark.renderer.markdown.MarkdownNodeRendererContext;
import org.commonmark.renderer.markdown.MarkdownWriter;

public class InsMarkdownNodeRenderer extends InsNodeRenderer {

    private final MarkdownNodeRendererContext context;
    private final MarkdownWriter writer;

    public InsMarkdownNodeRenderer(MarkdownNodeRendererContext context) {
        this.context = context;
        this.writer = context.getWriter();
    }

    @Override
    public void render(Node node) {
        writer.raw("++");
        renderChildren(node);
        writer.raw("++");
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
