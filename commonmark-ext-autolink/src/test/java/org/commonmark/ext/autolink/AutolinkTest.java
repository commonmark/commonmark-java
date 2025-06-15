package org.commonmark.ext.autolink;

import org.commonmark.Extension;
import org.commonmark.node.*;
import org.commonmark.parser.IncludeSourceSpans;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.testutil.RenderingTestCase;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(abc.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(0, 0, 0, 3)));

        assertThat(abc.getNext()).isInstanceOf(SoftLineBreak.class);

        Link one = (Link) abc.getNext().getNext();
        assertThat(one.getDestination()).isEqualTo("http://example.com/one");
        assertThat(one.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(1, 0, 4, 22)));

        assertThat(one.getNext()).isInstanceOf(SoftLineBreak.class);

        Text def = (Text) one.getNext().getNext();
        assertThat(def.getLiteral()).isEqualTo("def ");
        assertThat(def.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(2, 0, 27, 4)));

        Link two = (Link) def.getNext();
        assertThat(two.getDestination()).isEqualTo("http://example.com/two");
        assertThat(two.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(2, 4, 31, 22)));

        assertThat(two.getNext()).isInstanceOf(SoftLineBreak.class);

        Text ghi = (Text) two.getNext().getNext();
        assertThat(ghi.getLiteral()).isEqualTo("ghi ");
        assertThat(ghi.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(3, 0, 54, 4)));

        Link three = (Link) ghi.getNext();
        assertThat(three.getDestination()).isEqualTo("http://example.com/three");
        assertThat(three.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(3, 4, 58, 24)));

        Text jkl = (Text) three.getNext();
        assertThat(jkl.getLiteral()).isEqualTo(" jkl");
        assertThat(jkl.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(3, 28, 82, 4)));
    }

    @Override
    protected String render(String source) {
        return RENDERER.render(PARSER.parse(source));
    }
}
