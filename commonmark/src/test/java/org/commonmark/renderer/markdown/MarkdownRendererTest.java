package org.commonmark.renderer.markdown;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MarkdownRendererTest {

    // Leaf blocks

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
    public void testIndentedCodeBlocks() {
        assertRoundTrip("    hi\n");
        assertRoundTrip("    hi\n    code\n");
        assertRoundTrip(">     hi\n>     code\n");
    }

    @Test
    public void testFencedCodeBlocks() {
        assertRoundTrip("```\ntest\n```\n");
        assertRoundTrip("~~~~\ntest\n~~~~\n");
        assertRoundTrip("```info\ntest\n```\n");
        assertRoundTrip(" ```\n test\n ```\n");
        assertRoundTrip("```\n```\n");
    }

    @Test
    public void testHtmlBlocks() {
        assertRoundTrip("<div>test</div>\n");
    }

    @Test
    public void testParagraphs() {
        assertRoundTrip("foo\n");
        assertRoundTrip("foo\n\nbar\n");
    }

    // Container blocks

    @Test
    public void testBlockQuotes() {
        assertRoundTrip("> test\n");
        assertRoundTrip("> foo\n> bar\n");
        assertRoundTrip("> > foo\n> > bar\n");
        assertRoundTrip("> # Foo\n> \n> bar\n> baz\n");
    }

    @Test
    public void testBulletListItems() {
        assertRoundTrip("* foo\n");
        assertRoundTrip("- foo\n");
        assertRoundTrip("+ foo\n");
        assertRoundTrip("* foo\n  bar\n");
        assertRoundTrip("* ```\n  code\n  ```\n");
        assertRoundTrip("* foo\n\n* bar\n");
        // Note that the "  " in the second line is not necessary, but it's not wrong either.
        // We could try to avoid it in a future change, but not sure if necessary.
        assertRoundTrip("* foo\n  \n  bar\n");

        // Tight list
        assertRoundTrip("* foo\n* bar\n");
        // Tight list where the second item contains a loose list
        assertRoundTrip("- Foo\n  - Bar\n  \n  - Baz\n");

        // List item indent. This is a tricky one, but here the amount of space between the list marker and "one"
        // determines whether "two" is part of the list item or an indented code block.
        // In this case, it's an indented code block because it's not indented enough to be part of the list item.
        // If the renderer would just use "- one", then "two" would change from being an indented code block to being
        // a paragraph in the list item! So it is important for the renderer to preserve the content indent of the list
        // item.
        assertRoundTrip(" -    one\n\n     two\n");

        // Empty list
        assertRoundTrip("- \n\nFoo\n");
    }

    @Test
    public void testOrderedListItems() {
        assertRoundTrip("1. foo\n");
        assertRoundTrip("2. foo\n\n3. bar\n");

        // Tight list
        assertRoundTrip("1. foo\n2. bar\n");
        // Tight list where the second item contains a loose list
        assertRoundTrip("1. Foo\n   1. Bar\n   \n   2. Baz\n");

        assertRoundTrip(" 1.  one\n\n    two\n");
    }

    // Inlines

    @Test
    public void testEscaping() {
        // These are a bit tricky. We always escape some characters, even though they only need escaping if they would
        // otherwise result in a different parse result (e.g. a link):
        assertRoundTrip("\\[a\\](/uri)\n");
        assertRoundTrip("\\`abc\\`\n");
    }

    @Test
    public void testCodeSpans() {
        assertRoundTrip("`foo`\n");
        assertRoundTrip("``foo ` bar``\n");
        assertRoundTrip("```foo `` ` bar```\n");

        assertRoundTrip("`` `foo ``\n");
        assertRoundTrip("``  `  ``\n");
        assertRoundTrip("` `\n");
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
    public void testHtmlInline() {
        assertRoundTrip("<del>*foo*</del>\n");
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
        Node parsed = parse(source);
        return MarkdownRenderer.builder().build().render(parsed);
    }

    private void assertRoundTrip(String input) {
        String rendered = render(input);
        assertEquals(input, rendered);
    }
}
