package org.commonmark.test;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.testutil.SpecTestCase;
import org.commonmark.testutil.example.Example;
import org.junit.jupiter.api.Test;

import static org.commonmark.testutil.Asserts.assertRendering;

/**
 * Same as {@link SpecCoreTest} but converts line endings to Windows-style CR+LF endings before parsing.
 */
public class SpecCrLfCoreTest extends SpecTestCase {

    private static final Parser PARSER = Parser.builder().build();
    // The spec says URL-escaping is optional, but the examples assume that it's enabled.
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder().percentEncodeUrls(true).build();

    @Test
    public void testHtmlRendering() {
        assertRendering(example.getSource(), example.getHtml(), render(example.getSource()));
    }

    private String render(String source) {
        String windowsStyle = source.replace("\n", "\r\n");
        return RENDERER.render(PARSER.parse(windowsStyle));
    }
}
