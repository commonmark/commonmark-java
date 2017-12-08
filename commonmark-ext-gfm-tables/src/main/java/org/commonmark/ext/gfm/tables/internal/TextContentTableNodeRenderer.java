package org.commonmark.ext.gfm.tables.internal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.commonmark.ext.gfm.tables.TableBlock;
import org.commonmark.ext.gfm.tables.TableBody;
import org.commonmark.ext.gfm.tables.TableCell;
import org.commonmark.ext.gfm.tables.TableHead;
import org.commonmark.ext.gfm.tables.TableRow;
import org.commonmark.node.Node;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.text.TextContentNodeRendererContext;
import org.commonmark.renderer.text.TextContentWriter;

/**
 * The Table node renderer that is needed for rendering GFM tables (GitHub Flavored Markdown) to text.
 */
public class TextContentTableNodeRenderer implements NodeRenderer {

    private final TextContentWriter textContentWriter;
    private final TextContentNodeRendererContext context;

    public TextContentTableNodeRenderer(TextContentNodeRendererContext context) {
        this.textContentWriter = context.getWriter();
        this.context = context;
    }

    @Override
    public Set<Class<? extends Node>> getNodeTypes() {
        return new HashSet<>(Arrays.asList(
            TableBlock.class,
            TableHead.class,
            TableBody.class,
            TableRow.class,
            TableCell.class
        ));
    }

    @Override
    public void render(Node node) {
        // We don't render the table header (node instanceof TableHead) and its children for the text content.

        if (node instanceof TableBlock) {
            renderBlock((TableBlock) node);
        } else if (node instanceof TableHead) {
            renderHead((TableHead) node);
        } else if (node instanceof TableBody) {
            renderBody((TableBody) node);
        } else if (node instanceof TableRow) {
            renderRow((TableRow) node);
        } else if (node instanceof TableCell) {
            renderCell((TableCell) node);
        }
    }

    private void renderBlock(TableBlock tableBlock) {
        renderChildren(tableBlock);
        if (tableBlock.getNext() != null) {
            textContentWriter.write("\n");
        }
    }

    private void renderHead(TableHead tableHead) {
        renderChildren(tableHead);
    }

    private void renderBody(TableBody tableBody) {
        renderChildren(tableBody);
    }

    private void renderRow(TableRow tableRow) {
        textContentWriter.line();
        renderChildren(tableRow);
        textContentWriter.line();
    }

    private void renderCell(TableCell tableCell) {
        renderChildren(tableCell);
        textContentWriter.colon();
        textContentWriter.whitespace();
    }

    private void renderLastCell(TableCell tableCell) {
        renderChildren(tableCell);
    }

    private void renderChildren(Node parent) {
        Node node = parent.getFirstChild();
        while (node != null) {
            Node next = node.getNext();

            // For last cell in row, we dont render the delimiter.
            if (node instanceof TableCell && next == null) {
                renderLastCell((TableCell) node);
            } else {
                context.render(node);
            }

            node = next;
        }
    }
}
