package org.commonmark.ext.ins;

import org.commonmark.Extension;
import org.commonmark.node.Node;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SourceSpan;
import org.commonmark.parser.IncludeSourceSpans;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.text.TextContentRenderer;
import org.commonmark.testutil.RenderingTestCase;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class InsTest extends RenderingTestCase {

    private static final Set<Extension> EXTENSIONS = Set.of(InsExtension.create());
    private static final Parser PARSER = Parser.builder().extensions(EXTENSIONS).build();
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder().extensions(EXTENSIONS).build();
    private static final TextContentRenderer CONTENT_RENDERER = TextContentRenderer.builder()
            .extensions(EXTENSIONS).build();

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

    @Test
    public void textContentRenderer() {
        Node document = PARSER.parse("++foo++");
        assertEquals("foo", CONTENT_RENDERER.render(document));
    }

    @Test
    public void sourceSpans() {
        Parser parser = Parser.builder()
                .extensions(EXTENSIONS)
                .includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES)
                .build();

        Node document = parser.parse("hey ++there++\n");
        Paragraph block = (Paragraph) document.getFirstChild();
        Node ins = block.getLastChild();
        assertEquals(List.of(SourceSpan.of(0, 4, 4, 9)),
                ins.getSourceSpans());
    }

    @Override
    protected String render(String source) {
        return RENDERER.render(PARSER.parse(source));
    }
}
