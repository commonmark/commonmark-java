package org.commonmark.ext.ins.internal;

import org.commonmark.node.Node;
import org.commonmark.renderer.text.TextContentNodeRendererContext;

public class InsTextContentNodeRenderer extends InsNodeRenderer {

    private final TextContentNodeRendererContext context;

    public InsTextContentNodeRenderer(TextContentNodeRendererContext context) {
        this.context = context;
    }

    @Override
    public void render(Node node) {
        renderChildren(node);
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
