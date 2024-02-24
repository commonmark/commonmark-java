package org.commonmark.ext.gfm.tables.internal;

import org.commonmark.ext.gfm.tables.*;
import org.commonmark.node.Node;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.markdown.MarkdownNodeRendererContext;
import org.commonmark.renderer.markdown.MarkdownWriter;
import org.commonmark.text.AsciiMatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * The Table node renderer that is needed for rendering GFM tables (GitHub Flavored Markdown) to text content.
 */
public class TableMarkdownNodeRenderer extends TableNodeRenderer implements NodeRenderer {
    private final MarkdownWriter writer;
    private final MarkdownNodeRendererContext context;

    private final AsciiMatcher pipe = AsciiMatcher.builder().c('|').build();

    private final List<TableCell.Alignment> columns = new ArrayList<>();

    public TableMarkdownNodeRenderer(MarkdownNodeRendererContext context) {
        this.writer = context.getWriter();
        this.context = context;
    }

    @Override
    protected void renderBlock(TableBlock node) {
        columns.clear();
        writer.pushTight(true);
        renderChildren(node);
        writer.popTight();
        writer.block();
    }

    @Override
    protected void renderHead(TableHead node) {
        renderChildren(node);
        for (TableCell.Alignment columnAlignment : columns) {
            writer.raw('|');
            if (columnAlignment == TableCell.Alignment.LEFT) {
                writer.raw(":---");
            } else if (columnAlignment == TableCell.Alignment.RIGHT) {
                writer.raw("---:");
            } else if (columnAlignment == TableCell.Alignment.CENTER) {
                writer.raw(":---:");
            } else {
                writer.raw("---");
            }
        }
        writer.raw("|");
        writer.block();
    }

    @Override
    protected void renderBody(TableBody node) {
        renderChildren(node);
    }

    @Override
    protected void renderRow(TableRow node) {
        renderChildren(node);
        // Trailing | at the end of the line
        writer.raw("|");
        writer.block();
    }

    @Override
    protected void renderCell(TableCell node) {
        if (node.getParent() != null && node.getParent().getParent() instanceof TableHead) {
            columns.add(node.getAlignment());
        }
        writer.raw("|");
        writer.pushRawEscape(pipe);
        renderChildren(node);
        writer.popRawEscape();
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
