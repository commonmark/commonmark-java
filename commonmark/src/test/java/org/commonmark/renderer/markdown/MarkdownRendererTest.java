package org.commonmark.renderer.markdown;

import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.testutil.Asserts;
import org.junit.Test;

import static org.commonmark.testutil.Asserts.assertRendering;
import static org.junit.Assert.assertEquals;

public class MarkdownRendererTest {

    // Leaf blocks

    @Test
    public void testThematicBreaks() {
        assertRoundTrip("___\n");
        assertRoundTrip("___\n\nfoo\n");
        // List item with hr -> hr needs to not use the same as the marker
        assertRoundTrip("* ___\n");
        assertRoundTrip("- ___\n");

        // Preserve the literal
        assertRoundTrip("----\n");
        assertRoundTrip("*****\n");

        // Apply fallback for null literal
        ThematicBreak node = new ThematicBreak();
        assertEquals("___", render(node));
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

        assertRoundTrip("Foo\nbar\n===\n");
        assertRoundTrip("Foo  \nbar\n===\n");
        assertRoundTrip("[foo\nbar](/url)\n===\n");

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

        // Preserve the length
        assertRoundTrip("````\ntest\n````\n");
        assertRoundTrip("~~~\ntest\n~~~~~~\n");
    }

    @Test
    public void testFencedCodeBlocksFromAst() {
        var doc = new Document();
        var codeBlock = new FencedCodeBlock();
        codeBlock.setLiteral("hi code");
        doc.appendChild(codeBlock);

        assertRendering("", "```\nhi code\n```\n", render(doc));

        codeBlock.setLiteral("hi`\n```\n``test");
        assertRendering("", "````\nhi`\n```\n``test\n````\n", render(doc));
    }

    @Test
    public void testHtmlBlocks() {
        assertRoundTrip("<div>test</div>\n");
        assertRoundTrip("> <div>\n> test\n> </div>\n");
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
    public void testBulletListItemsFromAst() {
        var doc = new Document();
        var list = new BulletList();
        var item = new ListItem();
        item.appendChild(new Text("Test"));
        list.appendChild(item);
        doc.appendChild(list);

        assertRendering("", "- Test\n", render(doc));

        list.setMarker("*");
        assertRendering("", "* Test\n", render(doc));
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

    @Test
    public void testOrderedListItemsFromAst() {
        var doc = new Document();
        var list = new OrderedList();
        var item = new ListItem();
        item.appendChild(new Text("Test"));
        list.appendChild(item);
        doc.appendChild(list);

        assertRendering("", "1. Test\n", render(doc));

        list.setMarkerStartNumber(2);
        list.setMarkerDelimiter(")");
        assertRendering("", "2) Test\n", render(doc));
    }

    // Inlines

    @Test
    public void testTabs() {
        assertRoundTrip("a\tb\n");
    }

    @Test
    public void testEscaping() {
        // These are a bit tricky. We always escape some characters, even though they only need escaping if they would
        // otherwise result in a different parse result (e.g. a link):
        assertRoundTrip("\\[a\\](/uri)\n");
        assertRoundTrip("\\`abc\\`\n");

        // Some characters only need to be escaped at the beginning of the line
        assertRoundTrip("\\- Test\n");
        assertRoundTrip("\\-\n");
        assertRoundTrip("Test -\n");
        assertRoundTrip("Abc\n\n\\- Test\n");
        assertRoundTrip("\\# Test\n");
        assertRoundTrip("\\## Test\n");
        assertRoundTrip("\\#\n");
        assertRoundTrip("Foo\n\\===\n");
        // Only needs to be escaped after some text, not at beginning of paragraph
        assertRoundTrip("===\n");
        assertRoundTrip("a\n\n===\n");
        // The beginning of the line within the block, so disregarding prefixes
        assertRoundTrip("> \\- Test\n");
        assertRoundTrip("- \\- Test\n");
        // That's not the beginning of the line
        assertRoundTrip("`a`- foo\n");

        // This is a bit more tricky as we need to check for a list start
        assertRoundTrip("1\\. Foo\n");
        assertRoundTrip("999\\. Foo\n");
        assertRoundTrip("1\\.\n");
        assertRoundTrip("1\\) Foo\n");

        // Escaped whitespace, wow
        assertRoundTrip("&#9;foo\n");
        assertRoundTrip("&#32;   foo\n");
        assertRoundTrip("foo&#10;&#10;bar\n");
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
        assertRoundTrip("_*foo*_\n");

        // Not emphasis (needs * inside words)
        assertRoundTrip("foo\\_bar\\_\n");

        // Even when rendering a manually constructed tree, the emphasis delimiter needs to be chosen correctly.
        Document doc = new Document();
        Paragraph p = new Paragraph();
        doc.appendChild(p);
        Emphasis e1 = new Emphasis();
        p.appendChild(e1);
        Emphasis e2 = new Emphasis();
        e1.appendChild(e2);
        e2.appendChild(new Text("hi"));
        assertEquals("*_hi_*\n", render(doc));
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
        assertRoundTrip("[link](/uri \"tes\\\\\")\n");
        assertRoundTrip("[link](/url \"test&#10;&#10;\")\n");
        assertRoundTrip("[link](</url&#10;&#10;>)\n");
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

    private void assertRoundTrip(String input) {
        String rendered = parseAndRender(input);
        assertEquals(input, rendered);
    }

    private String parseAndRender(String source) {
        Node parsed = parse(source);
        return render(parsed);
    }

    private Node parse(String source) {
        return Parser.builder().build().parse(source);
    }

    private String render(Node node) {
        return MarkdownRenderer.builder().build().render(node);
    }
}
