package org.commonmark.ext.gfm.strikethrough.internal;

import org.commonmark.content.TextContentWriter;
import org.commonmark.content.renderer.TextContentNodeRendererContext;
import org.commonmark.node.Node;

public class StrikethroughTextContentNodeRenderer extends StrikethroughNodeRenderer {

    private final TextContentWriter textContent;

    public StrikethroughTextContentNodeRenderer(TextContentNodeRendererContext context) {
        super(context);
        this.textContent = context.getWriter();
    }

    @Override
    public void render(Node node) {
        textContent.write('/');
        renderChildren(node);
        textContent.write('/');
    }
}
