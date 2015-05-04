package org.commonmark.html;

import org.commonmark.node.Node;
import org.commonmark.node.Visitor;

public interface CustomHtmlRenderer {
    // TODO: maybe pass renderer instead of visitor?
    boolean render(Node node, HtmlWriter htmlWriter, Visitor visitor);
}

