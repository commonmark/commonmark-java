package org.commonmark.ext.footnotes;

import org.commonmark.Extension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.markdown.MarkdownRenderer;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertRoundTrip("Test [^foo]\n\n[^foo]: one\n\n[^bar]: two\n");
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
    public void testMultipleLines() {
        assertRoundTrip("Test [^1]\n\n[^1]: footnote l1\n    footnote l2\n");
    }

    @Test
    public void testMultipleParagraphs() {
        // Note that the line between p1 and p2 could be blank too (instead of 4 spaces), but we currently don't
        // preserve that information.
        assertRoundTrip("Test [^1]\n\n[^1]: footnote p1\n    \n    footnote p2\n");
    }

    @Test
    public void testInline() {
        assertRoundTrip("^[test *foo*]\n");
    }

    private void assertRoundTrip(String input) {
        String rendered = parseAndRender(input);
        assertThat(rendered).isEqualTo(input);
    }

    private String parseAndRender(String source) {
        Node parsed = PARSER.parse(source);
        return RENDERER.render(parsed);
    }
}
