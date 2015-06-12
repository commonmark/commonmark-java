package org.commonmark.ext.gfm.strikethrough;

import org.commonmark.html.CustomHtmlRenderer;
import org.commonmark.html.HtmlWriter;
import org.commonmark.node.Node;
import org.commonmark.node.Visitor;

public class StrikethroughHtmlRenderer implements CustomHtmlRenderer {

    @Override
    public boolean render(Node node, HtmlWriter htmlWriter, Visitor visitor) {
        if (node instanceof Strikethrough) {
            htmlWriter.tag("del");
            visitChildren(node, visitor);
            htmlWriter.tag("/del");
            return true;
        } else {
            return false;
        }
    }

    private void visitChildren(Node node, Visitor visitor) {
        Node child = node.getFirstChild();
        while (child != null) {
            child.accept(visitor);
            child = child.getNext();
        }
    }

}
