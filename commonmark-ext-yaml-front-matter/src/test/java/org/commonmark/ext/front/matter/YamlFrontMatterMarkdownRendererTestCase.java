package org.commonmark.ext.front.matter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.commonmark.Extension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.markdown.MarkdownRenderer;

abstract class YamlFrontMatterMarkdownRendererTestCase {
    protected final Parser parser = Parser.builder().extensions(getExtensions()).build();
    protected final MarkdownRenderer renderer =
            MarkdownRenderer.builder().extensions(getExtensions()).build();

    protected abstract Set<Extension> getExtensions();

    protected void assertRoundTrip(String input) {
        String rendered = renderer.render(parser.parse(input));
        assertThat(rendered).isEqualTo(input);
    }

    protected void assertRenderedEquals(Node inputNode, String expectedOutput) {
        var renderedOutput = renderer.render(inputNode);
        assertThat(renderedOutput).isEqualTo(expectedOutput);
    }
}
