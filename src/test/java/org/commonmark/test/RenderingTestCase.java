package org.commonmark.test;

import org.commonmark.html.HtmlRenderer;
import org.commonmark.Parser;
import org.commonmark.node.Node;

import static org.junit.Assert.assertEquals;

public abstract class RenderingTestCase {

    private final Parser parser = Parser.builder().build();
    private final HtmlRenderer renderer = HtmlRenderer.builder().build();

    protected void assertRendering(String source, String expectedHtml) {
        Node node = parser.parse(source);
        String html = renderer.render(node);

        // include source for better assertion errors
        String expected = expectedHtml + "\n\n" + source;
        String actual = html + "\n\n" + source;
        assertEquals(expected, actual);
    }

}
