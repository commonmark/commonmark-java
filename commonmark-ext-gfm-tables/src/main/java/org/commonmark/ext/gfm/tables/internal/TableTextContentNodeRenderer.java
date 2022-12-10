package org.commonmark.ext.gfm.tables.internal;

import org.commonmark.ext.gfm.tables.TableBlock;
import org.commonmark.ext.gfm.tables.TableBody;
import org.commonmark.ext.gfm.tables.TableCell;
import org.commonmark.ext.gfm.tables.TableHead;
import org.commonmark.ext.gfm.tables.TableRow;
import org.commonmark.node.Node;
import org.commonmark.node.Text;
import org.commonmark.renderer.text.TextContentNodeRendererContext;
import org.commonmark.renderer.text.TextContentWriter;

/**
 * The Table node renderer that is needed for rendering GFM tables (GitHub Flavored Markdown) to text content.
 */
public class TableTextContentNodeRenderer extends TableNodeRenderer {

    public static final char PIPE = '|';
    public static final String DELIMITER = "---";
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
        int columnCount = countColumns(tableHead);
        TableRow tableRow = constructDelimiterRow(columnCount);
        renderRow(tableRow);
    }

    private static TableRow constructDelimiterRow(int columnCount) {
        TableRow tableRow = new TableRow();
        for (int i = 0; i < columnCount; i++) {
            Text text = new Text(DELIMITER);
            TableCell tableCell = new TableCell();
            tableCell.appendChild(text);
            tableRow.appendChild(tableCell);
        }
        return tableRow;
    }

    private int countColumns(TableHead tableHead) {
        Node rowNode = tableHead.getFirstChild();
        if (rowNode == null) {
            return 0;
        }
        Node cellNode = rowNode.getFirstChild();

        int columnCount = 0;
        while (cellNode != null) {
            columnCount++;
            cellNode = cellNode.getNext();
        }
        return columnCount;
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
        textContentWriter.write(PIPE);
        textContentWriter.whitespace();
        renderChildren(tableCell);
        textContentWriter.whitespace();
    }

    private void renderLastCell(TableCell tableCell) {
        textContentWriter.write(PIPE);
        textContentWriter.whitespace();
        renderChildren(tableCell);
        textContentWriter.whitespace();
        textContentWriter.write(PIPE);
    }

    private void renderChildren(Node parent) {
        Node node = parent.getFirstChild();
        while (node != null) {
            Node next = node.getNext();

            // For last cell in row, we render the closing delimiter.
            if (node instanceof TableCell && next == null) {
                renderLastCell((TableCell) node);
            } else {
                context.render(node);
            }

            node = next;
        }
    }
}
