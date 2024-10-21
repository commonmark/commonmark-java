package org.commonmark.ext.gfm.strikethrough;

import org.commonmark.Extension;
import org.commonmark.node.Node;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SourceSpan;
import org.commonmark.node.Text;
import org.commonmark.parser.IncludeSourceSpans;
import org.commonmark.parser.Parser;
import org.commonmark.parser.delimiter.DelimiterProcessor;
import org.commonmark.parser.delimiter.DelimiterRun;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.text.TextContentRenderer;
import org.commonmark.testutil.RenderingTestCase;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class StrikethroughTest extends RenderingTestCase {

    private static final Set<Extension> EXTENSIONS = Set.of(StrikethroughExtension.create());
    private static final Parser PARSER = Parser.builder().extensions(EXTENSIONS).build();
    private static final HtmlRenderer HTML_RENDERER = HtmlRenderer.builder().extensions(EXTENSIONS).build();
    private static final TextContentRenderer CONTENT_RENDERER = TextContentRenderer.builder()
            .extensions(EXTENSIONS).build();

    @Test
    public void oneTildeIsEnough() {
        assertRendering("~foo~", "<p><del>foo</del></p>\n");
    }

    @Test
    public void twoTildesWorksToo() {
        assertRendering("~~foo~~", "<p><del>foo</del></p>\n");
    }

    @Test
    public void fourTildesNope() {
        assertRendering("foo ~~~~", "<p>foo ~~~~</p>\n");
    }

    @Test
    public void unmatched() {
        assertRendering("~~foo", "<p>~~foo</p>\n");
        assertRendering("foo~~", "<p>foo~~</p>\n");
    }

    @Test
    public void threeInnerThree() {
        assertRendering("a ~~~foo~~~", "<p>a ~~~foo~~~</p>\n");
    }

    @Test
    public void twoInnerThree() {
        assertRendering("~~foo~~~", "<p>~~foo~~~</p>\n");
    }

    @Test
    public void tildesInside() {
        assertRendering("~~foo~bar~~", "<p><del>foo~bar</del></p>\n");
        assertRendering("~~foo~~bar~~", "<p><del>foo</del>bar~~</p>\n");
        assertRendering("~~foo~~~bar~~", "<p><del>foo~~~bar</del></p>\n");
        assertRendering("~~foo~~~~bar~~", "<p><del>foo~~~~bar</del></p>\n");
        assertRendering("~~foo~~~~~bar~~", "<p><del>foo~~~~~bar</del></p>\n");
        assertRendering("~~foo~~~~~~bar~~", "<p><del>foo~~~~~~bar</del></p>\n");
    }

    @Test
    public void strikethroughWholeParagraphWithOtherDelimiters() {
        assertRendering("~~Paragraph with *emphasis* and __strong emphasis__~~",
                "<p><del>Paragraph with <em>emphasis</em> and <strong>strong emphasis</strong></del></p>\n");
    }

    @Test
    public void insideBlockQuote() {
        assertRendering("> strike ~~that~~",
                "<blockquote>\n<p>strike <del>that</del></p>\n</blockquote>\n");
    }

    @Test
    public void delimited() {
        Node document = PARSER.parse("~~foo~~");
        Strikethrough strikethrough = (Strikethrough) document.getFirstChild().getFirstChild();
        assertEquals("~~", strikethrough.getOpeningDelimiter());
        assertEquals("~~", strikethrough.getClosingDelimiter());
    }

    @Test
    public void textContentRenderer() {
        Node document = PARSER.parse("~~foo~~");
        assertEquals("/foo/", CONTENT_RENDERER.render(document));
    }

    @Test
    public void requireTwoTildesOption() {
        Parser parser = Parser.builder()
                .extensions(Set.of(StrikethroughExtension.builder()
                        .requireTwoTildes(true)
                        .build()))
                .customDelimiterProcessor(new SubscriptDelimiterProcessor())
                .build();

        Node document = parser.parse("~foo~ ~~bar~~");
        assertEquals("(sub)foo(/sub) /bar/", CONTENT_RENDERER.render(document));
    }

    @Test
    public void sourceSpans() {
        Parser parser = Parser.builder()
                .extensions(EXTENSIONS)
                .includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES)
                .build();

        Node document = parser.parse("hey ~~there~~\n");
        Paragraph block = (Paragraph) document.getFirstChild();
        Node strikethrough = block.getLastChild();
        assertEquals(List.of(SourceSpan.of(0, 4, 4, 9)),
                strikethrough.getSourceSpans());
    }

    @Override
    protected String render(String source) {
        return HTML_RENDERER.render(PARSER.parse(source));
    }

    private static class SubscriptDelimiterProcessor implements DelimiterProcessor {

        @Override
        public char getOpeningCharacter() {
            return '~';
        }

        @Override
        public char getClosingCharacter() {
            return '~';
        }

        @Override
        public int getMinLength() {
            return 1;
        }

        @Override
        public int process(DelimiterRun openingRun, DelimiterRun closingRun) {
            openingRun.getOpener().insertAfter(new Text("(sub)"));
            closingRun.getCloser().insertBefore(new Text("(/sub)"));
            return 1;
        }
    }
}
