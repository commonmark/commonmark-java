package org.commonmark.ext.gfm.tables.internal;

import org.commonmark.ext.gfm.tables.*;
import org.commonmark.internal.util.AsciiMatcher;
import org.commonmark.internal.util.CharMatcher;
import org.commonmark.node.Node;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.markdown.MarkdownNodeRendererContext;
import org.commonmark.renderer.markdown.MarkdownWriter;

import java.util.ArrayList;
import java.util.List;

/**
 * The Table node renderer that is needed for rendering GFM tables (GitHub Flavored Markdown) to text content.
 */
public class TableMarkdownNodeRenderer extends TableNodeRenderer implements NodeRenderer {
    private final MarkdownWriter writer;
    private final MarkdownNodeRendererContext context;

    private final CharMatcher pipe = AsciiMatcher.builder().c('|').build();

    private final List<TableCell.Alignment> columns = new ArrayList<>();

    public TableMarkdownNodeRenderer(MarkdownNodeRendererContext context) {
        this.writer = context.getWriter();
        this.context = context;
    }

    @Override
    protected void renderBlock(TableBlock node) {
        columns.clear();
        renderChildren(node);
        writer.block();
    }

    @Override
    protected void renderHead(TableHead node) {
        renderChildren(node);
        // TODO: Not sure about this.. Should block() detect if a line was already written? Or should line() itself be lazy?
        writer.line();
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
        // TODO
        if (node.getNext() != null) {
            writer.line();
        }
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
        // TODO
        if (node.getNext() != null) {
            writer.line();
        }
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
