package org.commonmark.ext.gfm.tables.internal;

import org.commonmark.ext.gfm.tables.TableBlock;
import org.commonmark.ext.gfm.tables.TableBody;
import org.commonmark.ext.gfm.tables.TableCell;
import org.commonmark.ext.gfm.tables.TableHead;
import org.commonmark.ext.gfm.tables.TableRow;
import org.commonmark.node.Node;
import org.commonmark.renderer.text.TextContentNodeRendererContext;
import org.commonmark.renderer.text.TextContentWriter;

/**
 * The Table node renderer that is needed for rendering GFM tables (GitHub Flavored Markdown) to text content.
 */
public class TableTextContentNodeRenderer extends TableNodeRenderer {

    private final TextContentWriter textContentWriter;
    private final TextContentNodeRendererContext context;

    public TableTextContentNodeRenderer(TextContentNodeRendererContext context) {
        this.textContentWriter = context.getWriter();
        this.context = context;
    }

    protected void renderBlock(TableBlock tableBlock) {
        renderChildren(tableBlock);
        if (tableBlock.getNext() != null) {
            textContentWriter.write("\n");
        }
    }

    protected void renderHead(TableHead tableHead) {
        renderChildren(tableHead);
    }

    protected void renderBody(TableBody tableBody) {
        renderChildren(tableBody);
    }

    protected void renderRow(TableRow tableRow) {
        textContentWriter.line();
        renderChildren(tableRow);
        textContentWriter.line();
    }

    protected void renderCell(TableCell tableCell) {
        renderChildren(tableCell);
        textContentWriter.pipe();
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
