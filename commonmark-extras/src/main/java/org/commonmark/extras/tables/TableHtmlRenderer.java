package org.commonmark.extras.tables;

import org.commonmark.html.CustomHtmlRenderer;
import org.commonmark.html.HtmlWriter;
import org.commonmark.node.Node;
import org.commonmark.node.Visitor;

import java.util.Collections;
import java.util.Map;

public class TableHtmlRenderer implements CustomHtmlRenderer {

    @Override
    public boolean render(Node node, HtmlWriter htmlWriter, Visitor visitor) {
        if (node instanceof TableBlock) {
            renderBlock((TableBlock) node, htmlWriter, visitor);
        } else if (node instanceof TableHead) {
            renderHead((TableHead) node, htmlWriter, visitor);
        } else if (node instanceof TableBody) {
            renderBody((TableBody) node, htmlWriter, visitor);
        } else if (node instanceof TableRow) {
            renderRow((TableRow) node, htmlWriter, visitor);
        } else if (node instanceof TableCell) {
            renderCell((TableCell) node, htmlWriter, visitor);
        } else {
            return false;
        }
        return true;
    }

    private void renderBlock(TableBlock tableBlock, HtmlWriter htmlWriter, Visitor visitor) {
        htmlWriter.line();
        // TODO: default attributes (sourcePos)
        htmlWriter.tag("table");
        visitChildren(tableBlock, visitor);
        htmlWriter.tag("/table");
        htmlWriter.line();
    }

    private void renderHead(TableHead tableHead, HtmlWriter htmlWriter, Visitor visitor) {
        htmlWriter.line();
        htmlWriter.tag("thead");
        visitChildren(tableHead, visitor);
        htmlWriter.tag("/thead");
        htmlWriter.line();
    }

    private void renderBody(TableBody tableBody, HtmlWriter htmlWriter, Visitor visitor) {
        htmlWriter.line();
        htmlWriter.tag("tbody");
        visitChildren(tableBody, visitor);
        htmlWriter.tag("/tbody");
        htmlWriter.line();
    }

    private void renderRow(TableRow tableRow, HtmlWriter htmlWriter, Visitor visitor) {
        htmlWriter.line();
        htmlWriter.tag("tr");
        visitChildren(tableRow, visitor);
        htmlWriter.tag("/tr");
        htmlWriter.line();
    }

    private void renderCell(TableCell tableCell, HtmlWriter htmlWriter, Visitor visitor) {
        String tag = tableCell.isHeader() ? "th" : "td";
        htmlWriter.tag(tag, getAttributes(tableCell));
        visitChildren(tableCell, visitor);
        htmlWriter.tag("/" + tag);
    }

    private static Map<String, String> getAttributes(TableCell tableCell) {
        if (tableCell.getAlignment() != null) {
            return Collections.singletonMap("align", getAlignValue(tableCell.getAlignment()));
        } else {
            return Collections.emptyMap();
        }
    }

    private static String getAlignValue(TableCell.Alignment alignment) {
        switch (alignment) {
            case LEFT:
                return "left";
            case CENTER:
                return "center";
            case RIGHT:
                return "right";
        }
        throw new IllegalStateException("Unknown alignment: " + alignment);
    }

    private void visitChildren(Node node, Visitor visitor) {
        Node child = node.getFirstChild();
        while (child != null) {
            child.accept(visitor);
            child = child.getNext();
        }
    }
}
