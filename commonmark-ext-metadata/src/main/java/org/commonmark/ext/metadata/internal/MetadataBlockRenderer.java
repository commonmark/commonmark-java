package org.commonmark.ext.metadata.internal;

import org.commonmark.ext.metadata.MetadataBlock;
import org.commonmark.ext.metadata.MetadataNode;
import org.commonmark.html.CustomHtmlRenderer;
import org.commonmark.html.HtmlWriter;
import org.commonmark.node.Node;
import org.commonmark.node.Visitor;

public class MetadataBlockRenderer implements CustomHtmlRenderer {
    @Override
    public boolean render(Node node, HtmlWriter htmlWriter, Visitor visitor) {
        return node instanceof MetadataBlock || node instanceof MetadataNode;
    }
}
