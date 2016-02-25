package org.commonmark.ext.front.matter.internal;

import org.commonmark.ext.front.matter.YamlFrontMatterBlock;
import org.commonmark.ext.front.matter.YamlFrontMatterNode;
import org.commonmark.html.CustomHtmlRenderer;
import org.commonmark.html.HtmlWriter;
import org.commonmark.node.Node;
import org.commonmark.node.Visitor;

public class YamlFrontMatterBlockRenderer implements CustomHtmlRenderer {
    @Override
    public boolean render(Node node, HtmlWriter htmlWriter, Visitor visitor) {
        return node instanceof YamlFrontMatterBlock || node instanceof YamlFrontMatterNode;
    }
}
