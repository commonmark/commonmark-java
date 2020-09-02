package org.commonmark.test;

import org.commonmark.node.BlockQuote;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.Heading;
import org.commonmark.node.HtmlBlock;
import org.commonmark.node.IndentedCodeBlock;
import org.commonmark.node.LinkReferenceDefinition;
import org.commonmark.node.ListBlock;
import org.commonmark.node.ListItem;
import org.commonmark.node.Node;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SourceSpan;
import org.commonmark.node.ThematicBreak;
import org.commonmark.parser.Parser;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.*;

public class SourceSpansTest {

    private static final Parser PARSER = Parser.builder().build();

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
        assertThat(paragraph.getSourceSpans(), contains(SourceSpan.of(3, 0, 3)));
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
        assertThat(listBlock.getSourceSpans(), contains(SourceSpan.of(1, 2, 5)));
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

    private String visualizeSourceSpans(String source) {
        Node document = PARSER.parse(source);
        return SourceSpanRenderer.render(document, source);
    }

    private static void assertSpans(String input, Class<? extends Node> nodeClass, SourceSpan... expectedSourceSpans) {
        Node node = PARSER.parse(input);
        while (node != null && !nodeClass.isInstance(node)) {
            node = node.getFirstChild();
        }
        if (node == null) {
            fail("Expected to find " + nodeClass + " node");
        } else {
            assertEquals(Arrays.asList(expectedSourceSpans), node.getSourceSpans());
        }
    }
}
