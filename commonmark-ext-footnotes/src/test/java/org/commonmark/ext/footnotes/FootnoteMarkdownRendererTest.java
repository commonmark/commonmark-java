package org.commonmark.ext.footnotes;

import org.commonmark.Extension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.markdown.MarkdownRenderer;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;

public class FootnoteMarkdownRendererTest {
    private static final Set<Extension> EXTENSIONS = Set.of(FootnotesExtension.builder().inlineFootnotes(true).build());
    private static final Parser PARSER = Parser.builder().extensions(EXTENSIONS).build();
    private static final MarkdownRenderer RENDERER = MarkdownRenderer.builder().extensions(EXTENSIONS).build();

    @Test
    public void testSimple() {
        assertRoundTrip("Test [^foo]\n\n[^foo]: note\n");
    }

    @Test
    public void testUnreferenced() {
        // Whether a reference has a corresponding definition or vice versa shouldn't matter for Markdown rendering.
        assertRoundTrip("Test [^foo]\n\n[^foo]: one\n[^bar]: two\n");
    }

    @Test
    public void testFootnoteWithBlock() {
        assertRoundTrip("Test [^foo]\n\n[^foo]: - foo\n    - bar\n");
    }

    @Test
    public void testBackslashInLabel() {
        assertRoundTrip("[^\\foo]\n\n[^\\foo]: note\n");
    }

    @Test
    public void testInline() {
        assertRoundTrip("^[test *foo*]\n");
    }

    private void assertRoundTrip(String input) {
        String rendered = parseAndRender(input);
        assertEquals(input, rendered);
    }

    private String parseAndRender(String source) {
        Node parsed = PARSER.parse(source);
        return RENDERER.render(parsed);
    }
}
