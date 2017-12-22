package org.commonmark.ext.autolink;

import org.commonmark.Extension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.testutil.RenderingTestCase;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;

public class AutolinkTest extends RenderingTestCase {

    private static final Set<Extension> EXTENSIONS = Collections.singleton(AutolinkExtension.create());
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

    @Override
    protected String render(String source) {
        return RENDERER.render(PARSER.parse(source));
    }
}
