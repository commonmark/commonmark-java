package org.commonmark.extras.test;

import org.commonmark.Parser;
import org.commonmark.extras.autolink.AutolinkPostProcessor;
import org.junit.Test;

public class AutolinkPostProcessorTest extends RenderingTestCase {

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

    @Override
    protected void configureParser(Parser.Builder parserBuilder) {
        parserBuilder.postProcessor(new AutolinkPostProcessor());
    }
}
