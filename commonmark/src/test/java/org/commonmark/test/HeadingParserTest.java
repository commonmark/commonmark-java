package org.commonmark.test;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.testutil.RenderingTestCase;
import org.junit.Test;

public class HeadingParserTest extends RenderingTestCase {

    private static final Parser PARSER = Parser.builder().build();
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder().build();

    @Test
    public void atxHeadingStart() {
        assertRendering("# test", "<h1>test</h1>\n");
        assertRendering("###### test", "<h6>test</h6>\n");
        assertRendering("####### test", "<p>####### test</p>\n");
        assertRendering("#test", "<p>#test</p>\n");
        assertRendering("#", "<h1></h1>\n");
    }

    @Test
    public void atxHeadingTrailing() {
        assertRendering("# test #", "<h1>test</h1>\n");
        assertRendering("# test ###", "<h1>test</h1>\n");
        assertRendering("# test # ", "<h1>test</h1>\n");
        assertRendering("# test  ###  ", "<h1>test</h1>\n");
        assertRendering("# test # #", "<h1>test #</h1>\n");
        assertRendering("# test#", "<h1>test#</h1>\n");
    }

    @Test
    public void atxHeadingSurrogates() {
        assertRendering("# \uD83D\uDE0A #", "<h1>\uD83D\uDE0A</h1>\n");
    }

    @Test
    public void setextHeadingMarkers() {
        assertRendering("test\n=", "<h1>test</h1>\n");
        assertRendering("test\n-", "<h2>test</h2>\n");
        assertRendering("test\n====", "<h1>test</h1>\n");
        assertRendering("test\n----", "<h2>test</h2>\n");
        assertRendering("test\n====   ", "<h1>test</h1>\n");
        assertRendering("test\n====   =", "<p>test\n====   =</p>\n");
        assertRendering("test\n=-=", "<p>test\n=-=</p>\n");
        assertRendering("test\n=a", "<p>test\n=a</p>\n");
    }

    @Override
    protected String render(String source) {
        return RENDERER.render(PARSER.parse(source));
    }
}
