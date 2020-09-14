package org.commonmark.test;

import org.commonmark.node.*;
import org.commonmark.parser.IncludeSourceSpans;
import org.commonmark.parser.Parser;
import org.junit.Test;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

import static org.junit.Assert.assertEquals;

public class SourceSpansTest {

    private static final Parser PARSER = Parser.builder().includeSourceSpans(IncludeSourceSpans.BLOCKS).build();
    private static final Parser INLINES_PARSER = Parser.builder().includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES).build();

    @Test
    public void paragraph() {
        assertSpans("foo\n", Paragraph.class, SourceSpan.of(0, 0, 3));
        assertSpans("foo\nbar\n", Paragraph.class, SourceSpan.of(0, 0, 3), SourceSpan.of(1, 0, 3));
        assertSpans("  foo\n  bar\n", Paragraph.class, SourceSpan.of(0, 0, 5), SourceSpan.of(1, 0, 5));
        assertSpans("> foo\n> bar\n", Paragraph.class, SourceSpan.of(0, 2, 3), SourceSpan.of(1, 2, 3));
        assertSpans("* foo\n  bar\n", Paragraph.class, SourceSpan.of(0, 2, 3), SourceSpan.of(1, 2, 3));
        assertSpans("* foo\nbar\n", Paragraph.class, SourceSpan.of(0, 2, 3), SourceSpan.of(1, 0, 3));
    }

    @Test
    public void thematicBreak() {
        assertSpans("---\n", ThematicBreak.class, SourceSpan.of(0, 0, 3));
        assertSpans("  ---\n", ThematicBreak.class, SourceSpan.of(0, 0, 5));
        assertSpans("> ---\n", ThematicBreak.class, SourceSpan.of(0, 2, 3));
    }

    @Test
    public void atxHeading() {
        assertSpans("# foo", Heading.class, SourceSpan.of(0, 0, 5));
        assertSpans(" # foo", Heading.class, SourceSpan.of(0, 0, 6));
        assertSpans("## foo ##", Heading.class, SourceSpan.of(0, 0, 9));
        assertSpans("> # foo", Heading.class, SourceSpan.of(0, 2, 5));
    }

    @Test
    public void setextHeading() {
        assertSpans("foo\n===\n", Heading.class, SourceSpan.of(0, 0, 3), SourceSpan.of(1, 0, 3));
        assertSpans("foo\nbar\n====\n", Heading.class, SourceSpan.of(0, 0, 3), SourceSpan.of(1, 0, 3), SourceSpan.of(2, 0, 4));
        assertSpans("  foo\n  ===\n", Heading.class, SourceSpan.of(0, 0, 5), SourceSpan.of(1, 0, 5));
        assertSpans("> foo\n> ===\n", Heading.class, SourceSpan.of(0, 2, 3), SourceSpan.of(1, 2, 3));
    }

    @Test
    public void indentedCodeBlock() {
        assertSpans("    foo\n", IndentedCodeBlock.class, SourceSpan.of(0, 0, 7));
        assertSpans("     foo\n", IndentedCodeBlock.class, SourceSpan.of(0, 0, 8));
        assertSpans("\tfoo\n", IndentedCodeBlock.class, SourceSpan.of(0, 0, 4));
        assertSpans(" \tfoo\n", IndentedCodeBlock.class, SourceSpan.of(0, 0, 5));
        assertSpans("  \tfoo\n", IndentedCodeBlock.class, SourceSpan.of(0, 0, 6));
        assertSpans("   \tfoo\n", IndentedCodeBlock.class, SourceSpan.of(0, 0, 7));
        assertSpans("    \tfoo\n", IndentedCodeBlock.class, SourceSpan.of(0, 0, 8));
        assertSpans("    \t foo\n", IndentedCodeBlock.class, SourceSpan.of(0, 0, 9));
        assertSpans("\t foo\n", IndentedCodeBlock.class, SourceSpan.of(0, 0, 5));
        assertSpans("\t  foo\n", IndentedCodeBlock.class, SourceSpan.of(0, 0, 6));
        assertSpans("    foo\n     bar\n", IndentedCodeBlock.class, SourceSpan.of(0, 0, 7), SourceSpan.of(1, 0, 8));
        assertSpans("    foo\n\tbar\n", IndentedCodeBlock.class, SourceSpan.of(0, 0, 7), SourceSpan.of(1, 0, 4));
        assertSpans("    foo\n    \n     \n", IndentedCodeBlock.class, SourceSpan.of(0, 0, 7), SourceSpan.of(1, 0, 4), SourceSpan.of(2, 0, 5));
        assertSpans(">     foo\n", IndentedCodeBlock.class, SourceSpan.of(0, 2, 7));
    }

    @Test
    public void fencedCodeBlock() {
        assertSpans("```\nfoo\n```\n", FencedCodeBlock.class,
                SourceSpan.of(0, 0, 3), SourceSpan.of(1, 0, 3), SourceSpan.of(2, 0, 3));
        assertSpans("```\n foo\n```\n", FencedCodeBlock.class,
                SourceSpan.of(0, 0, 3), SourceSpan.of(1, 0, 4), SourceSpan.of(2, 0, 3));
        assertSpans("```\nfoo\nbar\n```\n", FencedCodeBlock.class,
                SourceSpan.of(0, 0, 3), SourceSpan.of(1, 0, 3), SourceSpan.of(2, 0, 3), SourceSpan.of(3, 0, 3));
        assertSpans("```\nfoo\nbar\n```\n", FencedCodeBlock.class,
                SourceSpan.of(0, 0, 3), SourceSpan.of(1, 0, 3), SourceSpan.of(2, 0, 3), SourceSpan.of(3, 0, 3));
        assertSpans("   ```\n   foo\n   ```\n", FencedCodeBlock.class,
                SourceSpan.of(0, 0, 6), SourceSpan.of(1, 0, 6), SourceSpan.of(2, 0, 6));
        assertSpans(" ```\n foo\nfoo\n```\n", FencedCodeBlock.class,
                SourceSpan.of(0, 0, 4), SourceSpan.of(1, 0, 4), SourceSpan.of(2, 0, 3), SourceSpan.of(3, 0, 3));
        assertSpans("```info\nfoo\n```\n", FencedCodeBlock.class,
                SourceSpan.of(0, 0, 7), SourceSpan.of(1, 0, 3), SourceSpan.of(2, 0, 3));
        assertSpans("* ```\n  foo\n  ```\n", FencedCodeBlock.class,
                SourceSpan.of(0, 2, 3), SourceSpan.of(1, 2, 3), SourceSpan.of(2, 2, 3));
        assertSpans("> ```\n> foo\n> ```\n", FencedCodeBlock.class,
                SourceSpan.of(0, 2, 3), SourceSpan.of(1, 2, 3), SourceSpan.of(2, 2, 3));

        Node document = PARSER.parse("```\nfoo\n```\nbar\n");
        Paragraph paragraph = (Paragraph) document.getLastChild();
        assertEquals(Arrays.asList(SourceSpan.of(3, 0, 3)), paragraph.getSourceSpans());
    }

    @Test
    public void htmlBlock() {
        assertSpans("<div>\n", HtmlBlock.class, SourceSpan.of(0, 0, 5));
        assertSpans(" <div>\n foo\n </div>\n", HtmlBlock.class,
                SourceSpan.of(0, 0, 6),
                SourceSpan.of(1, 0, 4),
                SourceSpan.of(2, 0, 7));
        assertSpans("* <div>\n", HtmlBlock.class, SourceSpan.of(0, 2, 5));
    }

    @Test
    public void blockQuote() {
        assertSpans(">foo\n", BlockQuote.class, SourceSpan.of(0, 0, 4));
        assertSpans("> foo\n", BlockQuote.class, SourceSpan.of(0, 0, 5));
        assertSpans(">  foo\n", BlockQuote.class, SourceSpan.of(0, 0, 6));
        assertSpans(" > foo\n", BlockQuote.class, SourceSpan.of(0, 0, 6));
        assertSpans("   > foo\n  > bar\n", BlockQuote.class, SourceSpan.of(0, 0, 8), SourceSpan.of(1, 0, 7));
        // Lazy continuations
        assertSpans("> foo\nbar\n", BlockQuote.class, SourceSpan.of(0, 0, 5), SourceSpan.of(1, 0, 3));
        assertSpans("> foo\nbar\n> baz\n", BlockQuote.class, SourceSpan.of(0, 0, 5), SourceSpan.of(1, 0, 3), SourceSpan.of(2, 0, 5));
        assertSpans("> > foo\nbar\n", BlockQuote.class, SourceSpan.of(0, 0, 7), SourceSpan.of(1, 0, 3));
    }

    @Test
    public void listBlock() {
        assertSpans("* foo\n", ListBlock.class, SourceSpan.of(0, 0, 5));
        assertSpans("* foo\n  bar\n", ListBlock.class, SourceSpan.of(0, 0, 5), SourceSpan.of(1, 0, 5));
        assertSpans("* foo\n* bar\n", ListBlock.class, SourceSpan.of(0, 0, 5), SourceSpan.of(1, 0, 5));
        assertSpans("* foo\n  # bar\n", ListBlock.class, SourceSpan.of(0, 0, 5), SourceSpan.of(1, 0, 7));
        assertSpans("* foo\n  * bar\n", ListBlock.class, SourceSpan.of(0, 0, 5), SourceSpan.of(1, 0, 7));
        assertSpans("* foo\n> bar\n", ListBlock.class, SourceSpan.of(0, 0, 5));
        assertSpans("> * foo\n", ListBlock.class, SourceSpan.of(0, 2, 5));

        // Lazy continuations
        assertSpans("* foo\nbar\nbaz", ListBlock.class, SourceSpan.of(0, 0, 5), SourceSpan.of(1, 0, 3), SourceSpan.of(2, 0, 3));
        assertSpans("* foo\nbar\n* baz", ListBlock.class, SourceSpan.of(0, 0, 5), SourceSpan.of(1, 0, 3), SourceSpan.of(2, 0, 5));
        assertSpans("* foo\n  * bar\nbaz", ListBlock.class, SourceSpan.of(0, 0, 5), SourceSpan.of(1, 0, 7), SourceSpan.of(2, 0, 3));

        Node document = PARSER.parse("* foo\n  * bar\n");
        ListBlock listBlock = (ListBlock) document.getFirstChild().getFirstChild().getLastChild();
        assertEquals(Arrays.asList(SourceSpan.of(1, 2, 5)), listBlock.getSourceSpans());
    }

    @Test
    public void listItem() {
        assertSpans("* foo\n", ListItem.class, SourceSpan.of(0, 0, 5));
        assertSpans(" * foo\n", ListItem.class, SourceSpan.of(0, 0, 6));
        assertSpans("  * foo\n", ListItem.class, SourceSpan.of(0, 0, 7));
        assertSpans("   * foo\n", ListItem.class, SourceSpan.of(0, 0, 8));
        assertSpans("*\n  foo\n", ListItem.class, SourceSpan.of(0, 0, 1), SourceSpan.of(1, 0, 5));
        assertSpans("*\n  foo\n  bar\n", ListItem.class, SourceSpan.of(0, 0, 1), SourceSpan.of(1, 0, 5), SourceSpan.of(2, 0, 5));
        assertSpans("> * foo\n", ListItem.class, SourceSpan.of(0, 2, 5));

        // Lazy continuations
        assertSpans("* foo\nbar\n", ListItem.class, SourceSpan.of(0, 0, 5), SourceSpan.of(1, 0, 3));
        assertSpans("* foo\nbar\nbaz\n", ListItem.class, SourceSpan.of(0, 0, 5), SourceSpan.of(1, 0, 3), SourceSpan.of(2, 0, 3));
    }

    @Test
    public void linkReferenceDefinition() {
        // This is tricky due to how link reference definition parsing works. It is stripped from the paragraph if it's
        // successfully parsed, otherwise it stays part of the paragraph.
        Node document = PARSER.parse("[foo]: /url\ntext\n");

        LinkReferenceDefinition linkReferenceDefinition = (LinkReferenceDefinition) document.getFirstChild();
        assertEquals(Arrays.asList(SourceSpan.of(0, 0, 11)), linkReferenceDefinition.getSourceSpans());

        Paragraph paragraph = (Paragraph) document.getLastChild();
        assertEquals(Arrays.asList(SourceSpan.of(1, 0, 4)), paragraph.getSourceSpans());
    }

    @Test
    public void linkReferenceDefinitionHeading() {
        // This is probably the trickiest because we have a link reference definition at the start of a paragraph
        // that gets replaced because of a heading. Phew.
        Node document = PARSER.parse("[foo]: /url\nHeading\n===\n");

        LinkReferenceDefinition linkReferenceDefinition = (LinkReferenceDefinition) document.getFirstChild();
        assertEquals(Arrays.asList(SourceSpan.of(0, 0, 11)), linkReferenceDefinition.getSourceSpans());

        Heading heading = (Heading) document.getLastChild();
        assertEquals(Arrays.asList(SourceSpan.of(1, 0, 7), SourceSpan.of(2, 0, 3)), heading.getSourceSpans());
    }

    @Test
    public void visualCheck() {
        assertEquals("(> {[* <foo>]})\n(> {[  <bar>]})\n(> {⸢* ⸤baz⸥⸣})\n",
                visualizeSourceSpans("> * foo\n>   bar\n> * baz\n"));
        assertEquals("(> {[* <```>]})\n(> {[  <foo>]})\n(> {[  <```>]})\n",
                visualizeSourceSpans("> * ```\n>   foo\n>   ```"));
    }

    @Test
    public void inlineText() {
        assertInlineSpans("foo", Text.class, SourceSpan.of(0, 0, 3));
        assertInlineSpans("> foo", Text.class, SourceSpan.of(0, 2, 3));
        assertInlineSpans("* foo", Text.class, SourceSpan.of(0, 2, 3));

        // SourceSpans should be merged: ` is a separate Text node while inline parsing and gets merged at the end
        assertInlineSpans("foo`bar", Text.class, SourceSpan.of(0, 0, 7));
        assertInlineSpans("foo[bar", Text.class, SourceSpan.of(0, 0, 7));

        assertInlineSpans("[foo](/url)", Text.class, SourceSpan.of(0, 1, 3));
        assertInlineSpans("*foo*", Text.class, SourceSpan.of(0, 1, 3));
    }

    @Test
    public void inlineAutolink() {
        assertInlineSpans("see <https://example.org>", Link.class, SourceSpan.of(0, 4, 21));
    }

    @Test
    public void inlineBackslash() {
        assertInlineSpans("\\!", Text.class, SourceSpan.of(0, 0, 2));
    }

    @Test
    public void inlineBackticks() {
        assertInlineSpans("see `code`", Code.class, SourceSpan.of(0, 4, 6));
        assertInlineSpans("`multi\nline`", Code.class,
                SourceSpan.of(0, 0, 6),
                SourceSpan.of(1, 0, 5));
        assertInlineSpans("text ```", Text.class, SourceSpan.of(0, 0, 8));
    }

    @Test
    public void inlineEntity() {
        assertInlineSpans("&amp;", Text.class, SourceSpan.of(0, 0, 5));
    }

    @Test
    public void inlineHtml() {
        assertInlineSpans("hi <strong>there</strong>", HtmlInline.class, SourceSpan.of(0, 3, 8));
    }

    @Test
    public void links() {
        assertInlineSpans("[text](/url)", Link.class, SourceSpan.of(0, 0, 12));
        assertInlineSpans("[text](/url)", Text.class, SourceSpan.of(0, 1, 4));

        assertInlineSpans("[text]\n\n[text]: /url", Link.class, SourceSpan.of(0, 0, 6));
        assertInlineSpans("[text]\n\n[text]: /url", Text.class, SourceSpan.of(0, 1, 4));
        assertInlineSpans("[text][]\n\n[text]: /url", Link.class, SourceSpan.of(0, 0, 8));
        assertInlineSpans("[text][]\n\n[text]: /url", Text.class, SourceSpan.of(0, 1, 4));
        assertInlineSpans("[text][ref]\n\n[ref]: /url", Link.class, SourceSpan.of(0, 0, 11));
        assertInlineSpans("[text][ref]\n\n[ref]: /url", Text.class, SourceSpan.of(0, 1, 4));
        assertInlineSpans("[notalink]", Text.class, SourceSpan.of(0, 0, 10));
    }

    @Test
    public void inlineEmphasis() {
        assertInlineSpans("*hey*", Emphasis.class, SourceSpan.of(0, 0, 5));
        assertInlineSpans("*hey*", Text.class, SourceSpan.of(0, 1, 3));
        assertInlineSpans("**hey**", Emphasis.class, SourceSpan.of(0, 0, 7));
        assertInlineSpans("**hey**", Text.class, SourceSpan.of(0, 2, 3));
    }

    // TODO:
//    @Test
//    public void tabOffset() {
//        assertInlineSpans(">\t*foo*");
//    }

    private String visualizeSourceSpans(String source) {
        Node document = PARSER.parse(source);
        return SourceSpanRenderer.render(document, source);
    }


    private static void assertSpans(String input, Class<? extends Node> nodeClass, SourceSpan... expectedSourceSpans) {
        assertSpans(PARSER.parse(input), nodeClass, expectedSourceSpans);
    }

    private static void assertInlineSpans(String input, Class<? extends Node> nodeClass, SourceSpan... expectedSourceSpans) {
        assertSpans(INLINES_PARSER.parse(input), nodeClass, expectedSourceSpans);
    }

    private static void assertSpans(Node rootNode, Class<? extends Node> nodeClass, SourceSpan... expectedSourceSpans) {
        Node node = findNode(rootNode, nodeClass);
        assertEquals(Arrays.asList(expectedSourceSpans), node.getSourceSpans());
    }

    private static Node findNode(Node rootNode, Class<? extends Node> nodeClass) {
        Deque<Node> nodes = new ArrayDeque<>();
        nodes.add(rootNode);
        while (!nodes.isEmpty()) {
            Node node = nodes.removeFirst();
            if (nodeClass.isInstance(node)) {
                return node;
            }
            if (node.getFirstChild() != null) {
                nodes.addFirst(node.getFirstChild());
            }
            if (node.getNext() != null) {
                nodes.addLast(node.getNext());
            }
        }
        throw new AssertionError("Expected to find " + nodeClass + " node");
    }
}
