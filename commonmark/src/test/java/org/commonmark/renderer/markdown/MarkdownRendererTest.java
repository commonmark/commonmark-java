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

    @Test
    public void testEmphasis() {
        assertRoundTrip("*foo*\n");
        assertRoundTrip("foo*bar*\n");
        // When nesting, a different delimiter needs to be used
        assertRoundTrip("*_foo_*\n");
        assertRoundTrip("*_*foo*_*\n");

        // Not emphasis (needs * inside words)
        assertRoundTrip("foo_bar_\n");
    }

    @Test
    public void testStrongEmphasis() {
        assertRoundTrip("**foo**\n");
        assertRoundTrip("foo**bar**\n");
    }

    @Test
    public void testLinks() {
        assertRoundTrip("[link](/uri)\n");
        assertRoundTrip("[link](/uri \"title\")\n");
        assertRoundTrip("[link](</my uri>)\n");
        assertRoundTrip("[a](<b)c>)\n");
        assertRoundTrip("[a](<b(c>)\n");
        assertRoundTrip("[a](<b\\>c>)\n");
        assertRoundTrip("[a](<b\\\\\\>c>)\n");
        assertRoundTrip("[a](/uri \"foo \\\" bar\")\n");
    }

    @Test
    public void testImages() {
        assertRoundTrip("![link](/uri)\n");
        assertRoundTrip("![link](/uri \"title\")\n");
        assertRoundTrip("![link](</my uri>)\n");
        assertRoundTrip("![a](<b)c>)\n");
        assertRoundTrip("![a](<b(c>)\n");
        assertRoundTrip("![a](<b\\>c>)\n");
        assertRoundTrip("![a](<b\\\\\\>c>)\n");
        assertRoundTrip("![a](/uri \"foo \\\" bar\")\n");
    }

    @Test
    public void testHardLineBreaks() {
        assertRoundTrip("foo  \nbar\n");
    }

    @Test
    public void testSoftLineBreaks() {
        assertRoundTrip("foo\nbar\n");
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
