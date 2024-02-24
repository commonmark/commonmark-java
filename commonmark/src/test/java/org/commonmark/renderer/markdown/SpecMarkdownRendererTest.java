package org.commonmark.renderer.markdown;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.testutil.TestResources;
import org.commonmark.testutil.example.Example;
import org.commonmark.testutil.example.ExampleReader;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
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
    // The spec says URL-escaping is optional, but the examples assume that it's enabled.
    public static final HtmlRenderer HTML_RENDERER = HtmlRenderer.builder().percentEncodeUrls(true).build();

    @Test
    public void testCoverage() {
        List<Example> examples = ExampleReader.readExamples(TestResources.getSpec());
        List<Example> passes = new ArrayList<>();
        List<Example> fails = new ArrayList<>();
        for (Example example : examples) {
            String markdown = renderMarkdown(example.getSource());
            String rendered = renderHtml(markdown);
            if (rendered.equals(example.getHtml())) {
                passes.add(example);
            } else {
                fails.add(example);
            }
        }

        System.out.println("Passed examples by section (total " + passes.size() + "):");
        printCountsBySection(passes);
        System.out.println();

        System.out.println("Failed examples by section (total " + fails.size() + "):");
        printCountsBySection(fails);
        System.out.println();

        System.out.println("Failed examples:");
        for (Example fail : fails) {
            System.out.println("Failed: " + fail);
            System.out.println("````````````````````````````````");
            System.out.print(fail.getSource());
            System.out.println("````````````````````````````````");
            System.out.println();
        }

        int expectedPassed = 652;
        assertTrue("Expected at least " + expectedPassed + " examples to pass but was " + passes.size(), passes.size() >= expectedPassed);
        assertEquals(0, fails.size());
    }

    private static void printCountsBySection(List<Example> examples) {
        Map<String, Integer> bySection = new LinkedHashMap<>();
        for (Example example : examples) {
            Integer count = bySection.get(example.getSection());
            if (count == null) {
                count = 0;
            }
            bySection.put(example.getSection(), count + 1);
        }
        for (Map.Entry<String, Integer> entry : bySection.entrySet()) {
            System.out.println(entry.getValue() + ": " + entry.getKey());
        }
    }

    private Node parse(String source) {
        return Parser.builder().build().parse(source);
    }

    private String renderMarkdown(String source) {
        return MARKDOWN_RENDERER.render(parse(source));
    }

    private String renderHtml(String source) {
        // The spec uses "rightwards arrow" to show tabs
        return HTML_RENDERER.render(parse(source)).replace("\t", "\u2192");
    }
}
