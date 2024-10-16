package org.commonmark.ext.autolink;

import org.commonmark.Extension;
import org.commonmark.node.*;
import org.commonmark.parser.IncludeSourceSpans;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.testutil.RenderingTestCase;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AutolinkTest extends RenderingTestCase {

    private static final Set<Extension> EXTENSIONS = Set.of(AutolinkExtension.create());
    private static final Parser PARSER = Parser.builder().extensions(EXTENSIONS).build();
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder().extensions(EXTENSIONS).build();

    @Test
    public void oneTextNode() {
        assertRendering("foo http://one.org/ bar http://two.org/",
                "<p>foo <a href=\"http://one.org/\">http://one.org/</a> bar <a href=\"http://two.org/\">http://two.org/</a></p>\n");
    }

    @Test
    public void textNodeAndOthers() {
        assertRendering("foo http://one.org/ bar `code` baz http://two.org/",
                "<p>foo <a href=\"http://one.org/\">http://one.org/</a> bar <code>code</code> baz <a href=\"http://two.org/\">http://two.org/</a></p>\n");
    }

    @Test
    public void tricky() {
        assertRendering("http://example.com/one. Example 2 (see http://example.com/two). Example 3: http://example.com/foo_(bar)",
                "<p><a href=\"http://example.com/one\">http://example.com/one</a>. " +
                        "Example 2 (see <a href=\"http://example.com/two\">http://example.com/two</a>). " +
                        "Example 3: <a href=\"http://example.com/foo_(bar)\">http://example.com/foo_(bar)</a></p>\n");
    }

    @Test
    public void emailUsesMailto() {
        assertRendering("foo@example.com",
                "<p><a href=\"mailto:foo@example.com\">foo@example.com</a></p>\n");
    }

    @Test
    public void emailWithTldNotLinked() {
        assertRendering("foo@com",
                "<p>foo@com</p>\n");
    }

    @Test
    public void dontLinkTextWithinLinks() {
        assertRendering("<http://example.com>",
                "<p><a href=\"http://example.com\">http://example.com</a></p>\n");
    }

    @Test
    public void sourceSpans() {
        Parser parser = Parser.builder()
                .extensions(EXTENSIONS)
                .includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES)
                .build();
        Node document = parser.parse("abc\n" +
                "http://example.com/one\n" +
                "def http://example.com/two\n" +
                "ghi http://example.com/three jkl");

        Paragraph paragraph = (Paragraph) document.getFirstChild();
        Text abc = (Text) paragraph.getFirstChild();
        assertEquals(List.of(SourceSpan.of(0, 0, 0, 3)),
                abc.getSourceSpans());

        assertTrue(abc.getNext() instanceof SoftLineBreak);

        Link one = (Link) abc.getNext().getNext();
        assertEquals("http://example.com/one", one.getDestination());
        assertEquals(List.of(SourceSpan.of(1, 0, 4, 22)),
                one.getSourceSpans());

        assertTrue(one.getNext() instanceof SoftLineBreak);

        Text def = (Text) one.getNext().getNext();
        assertEquals("def ", def.getLiteral());
        assertEquals(List.of(SourceSpan.of(2, 0, 27, 4)),
                def.getSourceSpans());

        Link two = (Link) def.getNext();
        assertEquals("http://example.com/two", two.getDestination());
        assertEquals(List.of(SourceSpan.of(2, 4, 31, 22)),
                two.getSourceSpans());

        assertTrue(two.getNext() instanceof SoftLineBreak);

        Text ghi = (Text) two.getNext().getNext();
        assertEquals("ghi ", ghi.getLiteral());
        assertEquals(List.of(SourceSpan.of(3, 0, 54, 4)),
                ghi.getSourceSpans());

        Link three = (Link) ghi.getNext();
        assertEquals("http://example.com/three", three.getDestination());
        assertEquals(List.of(SourceSpan.of(3, 4, 58, 24)),
                three.getSourceSpans());

        Text jkl = (Text) three.getNext();
        assertEquals(" jkl", jkl.getLiteral());
        assertEquals(List.of(SourceSpan.of(3, 28, 82, 4)),
                jkl.getSourceSpans());
    }

    @Override
    protected String render(String source) {
        return RENDERER.render(PARSER.parse(source));
    }
}
