package org.commonmark.ext.autolink.internal;

import java.util.Set;
import org.commonmark.ext.autolink.BareAutoLink;
import org.commonmark.node.Node;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.text.TextContentNodeRendererContext;
import org.commonmark.renderer.text.TextContentWriter;

public class BareAutoLinkTextContentNodeRenderer implements NodeRenderer {

    private final TextContentWriter textContent;

    public BareAutoLinkTextContentNodeRenderer(TextContentNodeRendererContext context) {
        this.textContent = context.getWriter();
    }

    @Override
    public Set<Class<? extends Node>> getNodeTypes() {
        return Set.of(BareAutoLink.class);
    }

    @Override
    public void render(Node node) {
        textContent.write(((BareAutoLink) node).getContent());
    }
}
