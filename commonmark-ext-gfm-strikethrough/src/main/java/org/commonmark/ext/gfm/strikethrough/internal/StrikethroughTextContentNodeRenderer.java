package org.commonmark.ext.gfm.strikethrough.internal;

import org.commonmark.renderer.text.TextContentWriter;
import org.commonmark.renderer.text.TextContentNodeRendererContext;
import org.commonmark.node.Node;

public class StrikethroughTextContentNodeRenderer extends StrikethroughNodeRenderer {

    private final TextContentNodeRendererContext context;
    private final TextContentWriter textContent;

    public StrikethroughTextContentNodeRenderer(TextContentNodeRendererContext context) {
        this.context = context;
        this.textContent = context.getWriter();
    }

    @Override
    public void render(Node node) {
        textContent.write('/');
        renderChildren(node);
        textContent.write('/');
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
