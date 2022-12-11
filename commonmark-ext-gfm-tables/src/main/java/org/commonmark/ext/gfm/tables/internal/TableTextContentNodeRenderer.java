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
        TableRow tableRow = constructDelimiterRow(tableHead);
        renderRow(tableRow);
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

    private TableRow constructDelimiterRow(TableHead tableHead) {
        TableRow tableRow = new TableRow();

        Node rowNode = tableHead.getFirstChild();
        Node node = rowNode.getFirstChild();
        while (node != null) {
            TableCell cellNode = (TableCell) node;
            TableCell.Alignment alignment = cellNode.getAlignment();
            String delimiter = delimiterBy(alignment);

            Text text = new Text(delimiter);
            TableCell tableCell = new TableCell();
            tableCell.appendChild(text);
            tableRow.appendChild(tableCell);

            node = node.getNext();
        }

        return tableRow;
    }

    private String delimiterBy(TableCell.Alignment alignment) {
        if (alignment == null) {
            return "---";
        }
        switch (alignment) {
            case LEFT: return ":---";
            case RIGHT: return "---:";
            case CENTER: return ":---:";
            default: throw new IllegalStateException("Unknown alignment: " + alignment);
        }
    }
}
