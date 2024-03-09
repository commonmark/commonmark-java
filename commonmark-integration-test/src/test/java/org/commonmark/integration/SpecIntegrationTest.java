package org.commonmark.integration;

import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.parser.Parser;
import org.commonmark.testutil.example.Example;
import org.commonmark.testutil.SpecTestCase;
import org.junit.Test;

import java.util.*;

import static org.commonmark.testutil.Asserts.assertRendering;

/**
 * Tests that the spec examples still render the same with all extensions enabled.
 */
public class SpecIntegrationTest extends SpecTestCase {

    protected static final Parser PARSER = Parser.builder().extensions(Extensions.ALL_EXTENSIONS).build();
    // The spec says URL-escaping is optional, but the examples assume that it's enabled.
    protected static final HtmlRenderer RENDERER = HtmlRenderer.builder().extensions(Extensions.ALL_EXTENSIONS).percentEncodeUrls(true).build();
    protected static final Map<String, String> OVERRIDDEN_EXAMPLES = getOverriddenExamples();

    public SpecIntegrationTest(Example example) {
        super(example);
    }

    @Test
    public void testHtmlRendering() {
        String expectedHtml = OVERRIDDEN_EXAMPLES.get(example.getSource());
        if (expectedHtml != null) {
            assertRendering(example.getSource(), expectedHtml, render(example.getSource()));
        } else {
            assertRendering(example.getSource(), example.getHtml(), render(example.getSource()));
        }
    }

    protected String render(String source) {
        return RENDERER.render(PARSER.parse(source));
    }

    private static Map<String, String> getOverriddenExamples() {
        Map<String, String> m = new HashMap<>();

        // Not a spec autolink because of space, but the resulting text contains a valid URL
        m.put("<https://foo.bar/baz bim>\n", "<p>&lt;<a href=\"https://foo.bar/baz\">https://foo.bar/baz</a> bim&gt;</p>\n");

        // Not a spec autolink, but the resulting text contains a valid email
        m.put("<foo\\+@bar.example.com>\n", "<p>&lt;<a href=\"mailto:foo+@bar.example.com\">foo+@bar.example.com</a>&gt;</p>\n");

        // Not a spec autolink because of unknown scheme, but autolink extension doesn't limit schemes
        m.put("<heck://bing.bong>\n", "<p>&lt;<a href=\"heck://bing.bong%3E\">heck://bing.bong&gt;</a></p>\n");

        // Not a spec autolink because of spaces, but autolink extension doesn't limit schemes
        m.put("< https://foo.bar >\n", "<p>&lt; <a href=\"https://foo.bar\">https://foo.bar</a> &gt;</p>\n");

        // Plain autolink
        m.put("https://example.com\n", "<p><a href=\"https://example.com\">https://example.com</a></p>\n");

        // Plain autolink
        m.put("foo@bar.example.com\n", "<p><a href=\"mailto:foo@bar.example.com\">foo@bar.example.com</a></p>\n");

        // YAML front matter block
        m.put("---\nFoo\n---\nBar\n---\nBaz\n", "<h2>Bar</h2>\n<p>Baz</p>\n");
        m.put("---\n---\n", "");

        return m;
    }

}
