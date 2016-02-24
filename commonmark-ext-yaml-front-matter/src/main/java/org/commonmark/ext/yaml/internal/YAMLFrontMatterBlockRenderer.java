package org.commonmark.ext.yaml.internal;

import org.commonmark.ext.yaml.YAMLFrontMatterBlock;
import org.commonmark.ext.yaml.YAMLFrontMatterNode;
import org.commonmark.html.CustomHtmlRenderer;
import org.commonmark.html.HtmlWriter;
import org.commonmark.node.Node;
import org.commonmark.node.Visitor;

public class YAMLFrontMatterBlockRenderer implements CustomHtmlRenderer {
    @Override
    public boolean render(Node node, HtmlWriter htmlWriter, Visitor visitor) {
        return node instanceof YAMLFrontMatterBlock || node instanceof YAMLFrontMatterNode;
    }
}
