package org.commonmark.ext.autolink.internal;

import java.util.Set;
import org.commonmark.ext.autolink.BareAutoLink;
import org.commonmark.node.Node;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.markdown.MarkdownNodeRendererContext;
import org.commonmark.renderer.markdown.MarkdownWriter;

public class BareAutoLinkMarkdownNodeRenderer implements NodeRenderer {

    private final MarkdownWriter writer;

    public BareAutoLinkMarkdownNodeRenderer(MarkdownNodeRendererContext context) {
        this.writer = context.getWriter();
    }

    @Override
    public Set<Class<? extends Node>> getNodeTypes() {
        return Set.of(BareAutoLink.class);
    }

    @Override
    public void render(Node node) {
        // Bare autolinks round-trip as their literal content — no angle brackets, no prefix.
        // The destination prefix (mailto:, http://) is a rendering concern, not source.
        writer.raw(((BareAutoLink) node).getContent());
    }
}
