package org.commonmark.integration;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.testutil.RenderingTestCase;
import org.junit.Test;

/**
 * Tests to ensure all extensions work well together.
 */
public class ExtensionsIntegrationTest extends RenderingTestCase {

    protected static final Parser PARSER = Parser.builder()
            .extensions(Extensions.ALL_EXTENSIONS)
            .build();
    protected static final HtmlRenderer RENDERER = HtmlRenderer.builder()
            .extensions(Extensions.ALL_EXTENSIONS)
            .percentEncodeUrls(true)
            .build();

    @Test
    public void testImageAttributes() {
        assertRendering("![text](/url.png){height=5 width=6}", "<p><img src=\"/url.png\" alt=\"text\" height=\"5\" width=\"6\" /></p>\n");
    }

    @Test
    public void testTaskListItems() {
        assertRendering("- [ ] task to do\n- [x] task done\n",
                "<ul>\n<li><input type=\"checkbox\" disabled=\"\"> task to do</li>\n" +
                        "<li><input type=\"checkbox\" disabled=\"\" checked=\"\"> task done</li>\n</ul>\n");

    }

    protected String render(String source) {
        return RENDERER.render(PARSER.parse(source));
    }
}
