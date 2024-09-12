package org.commonmark.ext.footnotes.internal;

import org.commonmark.ext.footnotes.FootnoteDefinition;
import org.commonmark.ext.footnotes.FootnoteReference;
import org.commonmark.ext.footnotes.InlineFootnote;
import org.commonmark.node.*;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.renderer.html.HtmlWriter;
import org.commonmark.renderer.markdown.MarkdownNodeRendererContext;
import org.commonmark.renderer.markdown.MarkdownWriter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class FootnoteMarkdownNodeRenderer implements NodeRenderer {

    private final MarkdownWriter writer;
    private final MarkdownNodeRendererContext context;

    public FootnoteMarkdownNodeRenderer(MarkdownNodeRendererContext context) {
        this.writer = context.getWriter();
        this.context = context;
    }

    @Override
    public Set<Class<? extends Node>> getNodeTypes() {
        return Set.of(FootnoteReference.class, InlineFootnote.class, FootnoteDefinition.class);
    }

    @Override
    public void render(Node node) {
        if (node instanceof FootnoteReference) {
            renderReference((FootnoteReference) node);
        } else if (node instanceof InlineFootnote) {
            renderInline((InlineFootnote) node);
        } else if (node instanceof FootnoteDefinition) {
            renderDefinition((FootnoteDefinition) node);
        }
    }

    private void renderReference(FootnoteReference ref) {
        writer.raw("[^");
        // The label is parsed as-is without escaping, so we can render it back as-is
        writer.raw(ref.getLabel());
        writer.raw("]");
    }

    private void renderInline(InlineFootnote inlineFootnote) {
        writer.raw("^[");
        renderChildren(inlineFootnote);
        writer.raw("]");
    }

    private void renderDefinition(FootnoteDefinition def) {
        writer.raw("[^");
        writer.raw(def.getLabel());
        writer.raw("]: ");

        writer.pushPrefix("    ");
        writer.pushTight(true);
        renderChildren(def);
        writer.popTight();
        writer.popPrefix();
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
