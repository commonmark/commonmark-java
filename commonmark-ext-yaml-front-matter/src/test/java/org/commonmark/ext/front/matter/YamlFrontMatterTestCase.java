package org.commonmark.ext.front.matter;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.commonmark.Extension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.testutil.RenderingTestCase;

abstract class YamlFrontMatterTestCase extends RenderingTestCase {
    protected final Parser parser = Parser.builder().extensions(getExtensions()).build();
    protected final HtmlRenderer renderer =
            HtmlRenderer.builder().extensions(getExtensions()).build();

    abstract Set<Extension> getExtensions();

    @Override
    protected String render(String source) {
        return renderer.render(parser.parse(source));
    }

    protected String getFrontMatterContent(String input) {
        Node document = parser.parse(input);

        return YamlFrontMatterVisitor.readRawContent(document);
    }

    protected Map<String, List<String>> getFrontMatterData(String input) {
        Node document = parser.parse(input);

        return YamlFrontMatterVisitor.readData(document);
    }
}
