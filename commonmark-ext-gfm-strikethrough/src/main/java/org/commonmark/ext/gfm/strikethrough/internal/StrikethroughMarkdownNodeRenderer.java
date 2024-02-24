package org.commonmark.ext.gfm.strikethrough.internal;

import org.commonmark.ext.gfm.strikethrough.Strikethrough;
import org.commonmark.node.Node;
import org.commonmark.renderer.markdown.MarkdownNodeRendererContext;
import org.commonmark.renderer.markdown.MarkdownWriter;

public class StrikethroughMarkdownNodeRenderer extends StrikethroughNodeRenderer {

    private final MarkdownNodeRendererContext context;
    private final MarkdownWriter writer;

    public StrikethroughMarkdownNodeRenderer(MarkdownNodeRendererContext context) {
        this.context = context;
        this.writer = context.getWriter();
    }

    @Override
    public void render(Node node) {
        Strikethrough strikethrough = (Strikethrough) node;
        writer.raw(strikethrough.getOpeningDelimiter());
        renderChildren(node);
        writer.raw(strikethrough.getClosingDelimiter());
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
