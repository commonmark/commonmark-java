package org.commonmark.ext.heading.anchor;

import org.commonmark.Extension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.testutil.RenderingTestCase;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;

public class HeadingAnchorTest extends RenderingTestCase {

    private static final Set<Extension> EXTENSIONS = Collections.singleton(HeadingAnchorExtension.create());
    private static final Parser PARSER = Parser.builder().build();
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder().extensions(EXTENSIONS).build();

    @Test
    public void baseCaseSingleHeader() {
        assertRendering("# Heading here\n",
                "<h1 id=\"heading-here\">Heading here</h1>\n");
    }

    @Test
    public void singleHeaderWithCodeBlock() {
        assertRendering("Hi there\n# Heading `here`\n",
                "<p>Hi there</p>\n<h1 id=\"heading-here\">Heading <code>here</code></h1>\n");
    }

    @Test
    public void duplicateHeadersMakeUniqueIds() {
        assertRendering("# Heading here\n# Heading here",
                "<h1 id=\"heading-here\">Heading here</h1>\n<h1 id=\"heading-here-1\">Heading here</h1>\n");
    }

    @Test
    public void testSupplementalDiacriticalMarks() {
        assertRendering("# a\u1DC0", "<h1 id=\"a\u1DC0\">a\u1DC0</h1>\n");
    }

    @Test
    public void testUndertieUnicodeDisplayed() {
        assertRendering("# undertie \u203F", "<h1 id=\"undertie-\u203F\">undertie \u203F</h1>\n");
    }

    @Test
    public void testExplicitHeaderCollision() {
        assertRendering("# Header\n# Header\n# Header-1",
                "<h1 id=\"header\">Header</h1>\n" +
                        "<h1 id=\"header-1\">Header</h1>\n" +
                        "<h1 id=\"header-1\">Header-1</h1>\n");
    }

    @Test
    public void testCaseIsIgnoredWhenComparingIds() {
        assertRendering("# HEADING here\n" +
                        "# heading here",
                "<h1 id=\"heading-here\">HEADING here</h1>\n" +
                        "<h1 id=\"heading-here-1\">heading here</h1>\n");
    }

    @Test
    public void testNestedBlocks() {
        assertRendering("## `h` `e` **l** *l* o",
                "<h2 id=\"h-e-l-l-o\"><code>h</code> <code>e</code> <strong>l</strong> <em>l</em> o</h2>\n");
    }

    @Test
    public void boldEmphasisCharacters() {
        assertRendering("# _hello_ **there**", "<h1 id=\"hello-there\"><em>hello</em> <strong>there</strong></h1>\n");
    }

    @Test
    public void testStrongEmphasis() {
        assertRendering("# _**Hi there**_", "<h1 id=\"hi-there\"><em><strong>Hi there</strong></em></h1>\n");
    }

    @Test
    public void testMultipleSpacesKept() {
        assertRendering("# Hi  There", "<h1 id=\"hi--there\">Hi  There</h1>\n");
    }

    @Test
    public void testNonAsciiCharacterHeading() {
        assertRendering("# bär", "<h1 id=\"bär\">bär</h1>\n");
    }

    @Test
    public void testCombiningDiaeresis() {
        assertRendering("# Product\u036D\u036B", "<h1 id=\"product\u036D\u036B\">Product\u036D\u036B</h1>\n");
    }

    @Override
    protected String render(String source) {
        return RENDERER.render(PARSER.parse(source));
    }
}
