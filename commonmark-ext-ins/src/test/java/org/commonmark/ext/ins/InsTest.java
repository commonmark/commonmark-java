package org.commonmark.ext.ins;

import org.commonmark.Extension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.testutil.RenderingTestCase;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class InsTest extends RenderingTestCase {

    private static final Set<Extension> EXTENSIONS = Collections.singleton(InsExtension.create());
    private static final Parser PARSER = Parser.builder().extensions(EXTENSIONS).build();
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder().extensions(EXTENSIONS).build();

    @Test
    public void onePlusIsNotEnough() {
        assertRendering("+foo+", "<p>+foo+</p>\n");
    }

    @Test
    public void twoPlusesYay() {
        assertRendering("++foo++", "<p><ins>foo</ins></p>\n");
    }

    @Test
    public void fourPlusesNope() {
        assertRendering("foo ++++", "<p>foo ++++</p>\n");
    }

    @Test
    public void unmatched() {
        assertRendering("++foo", "<p>++foo</p>\n");
        assertRendering("foo++", "<p>foo++</p>\n");
    }

    @Test
    public void threeInnerThree() {
        assertRendering("+++foo+++", "<p>+<ins>foo</ins>+</p>\n");
    }

    @Test
    public void twoInnerThree() {
        assertRendering("++foo+++", "<p><ins>foo</ins>+</p>\n");
    }

    @Test
    public void plusesInside() {
        assertRendering("++foo+bar++", "<p><ins>foo+bar</ins></p>\n");
        assertRendering("++foo++bar++", "<p><ins>foo</ins>bar++</p>\n");
        assertRendering("++foo+++bar++", "<p><ins>foo</ins>+bar++</p>\n");
        assertRendering("++foo++++bar++", "<p><ins>foo</ins><ins>bar</ins></p>\n");
        assertRendering("++foo+++++bar++", "<p><ins>foo</ins>+<ins>bar</ins></p>\n");
        assertRendering("++foo++++++bar++", "<p><ins>foo</ins>++<ins>bar</ins></p>\n");
        assertRendering("++foo+++++++bar++", "<p><ins>foo</ins>+++<ins>bar</ins></p>\n");
    }

    @Test
    public void insWholeParagraphWithOtherDelimiters() {
        assertRendering("++Paragraph with *emphasis* and __strong emphasis__++",
                "<p><ins>Paragraph with <em>emphasis</em> and <strong>strong emphasis</strong></ins></p>\n");
    }

    @Test
    public void insideBlockQuote() {
        assertRendering("> underline ++that++",
                "<blockquote>\n<p>underline <ins>that</ins></p>\n</blockquote>\n");
    }

    @Test
    public void delimited() {
        Node document = PARSER.parse("++foo++");
        Ins ins = (Ins) document.getFirstChild().getFirstChild();
        assertEquals("++", ins.getOpeningDelimiter());
        assertEquals("++", ins.getClosingDelimiter());
    }

    @Override
    protected String render(String source) {
        return RENDERER.render(PARSER.parse(source));
    }
}
