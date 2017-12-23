package org.commonmark.ext.gfm.tables.internal;

import org.commonmark.ext.gfm.tables.*;
import org.commonmark.node.Node;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.renderer.html.HtmlWriter;

import java.util.*;

public class TableNodeRenderer implements NodeRenderer {

    private final HtmlWriter htmlWriter;
    private final HtmlNodeRendererContext context;

    public TableNodeRenderer(HtmlNodeRendererContext context) {
        this.htmlWriter = context.getWriter();
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
        htmlWriter.line();
        htmlWriter.tag("table", getAttributes(tableBlock, "table"));
        renderChildren(tableBlock);
        htmlWriter.tag("/table");
        htmlWriter.line();
    }

    private void renderHead(TableHead tableHead) {
        htmlWriter.line();
        htmlWriter.tag("thead", getAttributes(tableHead, "thead"));
        renderChildren(tableHead);
        htmlWriter.tag("/thead");
        htmlWriter.line();
    }

    private void renderBody(TableBody tableBody) {
        htmlWriter.line();
        htmlWriter.tag("tbody", getAttributes(tableBody, "tbody"));
        renderChildren(tableBody);
        htmlWriter.tag("/tbody");
        htmlWriter.line();
    }

    private void renderRow(TableRow tableRow) {
        htmlWriter.line();
        htmlWriter.tag("tr", getAttributes(tableRow, "tr"));
        renderChildren(tableRow);
        htmlWriter.tag("/tr");
        htmlWriter.line();
    }

    private void renderCell(TableCell tableCell) {
        String tagName = tableCell.isHeader() ? "th" : "td";
        htmlWriter.tag(tagName, getCellAttributes(tableCell, tagName));
        renderChildren(tableCell);
        htmlWriter.tag("/" + tagName);
    }

    private Map<String, String> getAttributes(Node node, String tagName) {
        return context.extendAttributes(node, tagName, Collections.<String, String>emptyMap());
    }

    private Map<String, String> getCellAttributes(TableCell tableCell, String tagName) {
        if (tableCell.getAlignment() != null) {
            return context.extendAttributes(tableCell, tagName, Collections.singletonMap("align", getAlignValue(tableCell.getAlignment())));
        } else {
            return context.extendAttributes(tableCell, tagName, Collections.<String, String>emptyMap());
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

    private void renderChildren(Node parent) {
        Node node = parent.getFirstChild();
        while (node != null) {
            Node next = node.getNext();
            context.render(node);
            node = next;
        }
    }
}
