package org.commonmark.renderer.markdown;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MarkdownRendererTest {

    @Test
    public void testThematicBreaks() {
        assertRoundTrip("***\n");
        // TODO: spec: If you want a thematic break in a list item, use a different bullet:

        assertRoundTrip("***\n\nfoo\n");
    }

    @Test
    public void testHeadings() {
        // Type of heading is currently not preserved
        assertRoundTrip("# foo\n");
        assertRoundTrip("## foo\n");
        assertRoundTrip("### foo\n");
        assertRoundTrip("#### foo\n");
        assertRoundTrip("##### foo\n");
        assertRoundTrip("###### foo\n");

        assertRoundTrip("# foo\n\nbar\n");
    }

    @Test
    public void testParagraphs() {
        assertRoundTrip("foo\n");
        assertRoundTrip("foo\n\nbar\n");
    }

    @Test
    public void testCodeSpans() {
        assertRoundTrip("`foo`\n");
        assertRoundTrip("``foo ` bar``\n");
        assertRoundTrip("```foo `` ` bar```\n");

        assertRoundTrip("`` `foo ``\n");
    }

    private Node parse(String source) {
        return Parser.builder().build().parse(source);
    }

    private String render(String source) {
        return MarkdownRenderer.builder().build().render(parse(source));
    }

    private void assertRoundTrip(String input) {
        String rendered = render(input);
        assertEquals(input, rendered);
    }
}
