package org.commonmark.renderer.markdown;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.testutil.TestResources;
import org.commonmark.testutil.example.Example;
import org.commonmark.testutil.example.ExampleReader;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Tests Markdown rendering using the examples in the spec like this:
 * <ol>
 * <li>Parses the source to an AST and then renders it back to Markdown</li>
 * <li>Parses that to an AST and then renders it to HTML</li>
 * <li>Compares that HTML to the expected HTML of the example:
 * If it's the same, then the expected elements were preserved in the Markdown rendering</li>
 * </ol>
 */
public class SpecMarkdownRendererTest {

    public static final MarkdownRenderer MARKDOWN_RENDERER = MarkdownRenderer.builder().build();
    public static final HtmlRenderer HTML_RENDERER = HtmlRenderer.builder().build();

    @Test
    public void testCoverage() {
        List<Example> examples = ExampleReader.readExamples(TestResources.getSpec());
        int passed = 0;
        for (Example example : examples) {
            String markdown = renderMarkdown(example.getSource());
            String rendered = renderHtml(markdown);
            if (rendered.equals(example.getHtml())) {
                passed++;
            }
        }

        int expectedPassed = 151;
        assertTrue("Expected at least " + expectedPassed + " examples to pass but was " + passed, passed >= expectedPassed);
    }

    private Node parse(String source) {
        return Parser.builder().build().parse(source);
    }

    private String renderMarkdown(String source) {
        return MARKDOWN_RENDERER.render(parse(source));
    }

    private String renderHtml(String source) {
        return HTML_RENDERER.render(parse(source));
    }
}
