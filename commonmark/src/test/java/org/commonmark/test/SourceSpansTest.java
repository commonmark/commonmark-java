package org.commonmark.test;

import org.commonmark.node.*;
import org.commonmark.parser.IncludeSourceSpans;
import org.commonmark.parser.Parser;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SourceSpansTest {

    private static final Parser PARSER = Parser.builder().includeSourceSpans(IncludeSourceSpans.BLOCKS).build();
    private static final Parser INLINES_PARSER = Parser.builder().includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES).build();

    @Test
    public void paragraph() {
        assertSpans("foo\n", Paragraph.class, SourceSpan.of(0, 0, 0, 3));
        assertSpans("foo\nbar\n", Paragraph.class, SourceSpan.of(0, 0, 0, 3), SourceSpan.of(1, 0, 4, 3));
        assertSpans("  foo\n  bar\n", Paragraph.class, SourceSpan.of(0, 0, 0, 5), SourceSpan.of(1, 0, 6, 5));
        assertSpans("> foo\n> bar\n", Paragraph.class, SourceSpan.of(0, 2, 2, 3), SourceSpan.of(1, 2, 8, 3));
        assertSpans("* foo\n  bar\n", Paragraph.class, SourceSpan.of(0, 2, 2, 3), SourceSpan.of(1, 2, 8, 3));
        assertSpans("* foo\nbar\n", Paragraph.class, SourceSpan.of(0, 2, 2, 3), SourceSpan.of(1, 0, 6, 3));
    }

    @Test
    public void thematicBreak() {
        assertSpans("---\n", ThematicBreak.class, SourceSpan.of(0, 0, 0, 3));
        assertSpans("  ---\n", ThematicBreak.class, SourceSpan.of(0, 0, 0, 5));
        assertSpans("> ---\n", ThematicBreak.class, SourceSpan.of(0, 2, 2, 3));
    }

    @Test
    public void atxHeading() {
        assertSpans("# foo", Heading.class, SourceSpan.of(0, 0, 0, 5));
        assertSpans(" # foo", Heading.class, SourceSpan.of(0, 0, 0, 6));
        assertSpans("## foo ##", Heading.class, SourceSpan.of(0, 0, 0, 9));
        assertSpans("> # foo", Heading.class, SourceSpan.of(0, 2, 2, 5));
    }

    @Test
    public void setextHeading() {
        assertSpans("foo\n===\n", Heading.class, SourceSpan.of(0, 0, 0, 3), SourceSpan.of(1, 0, 4, 3));
        assertSpans("foo\nbar\n====\n", Heading.class, SourceSpan.of(0, 0, 0, 3), SourceSpan.of(1, 0, 4, 3), SourceSpan.of(2, 0, 8, 4));
        assertSpans("  foo\n  ===\n", Heading.class, SourceSpan.of(0, 0, 0, 5), SourceSpan.of(1, 0, 6, 5));
        assertSpans("> foo\n> ===\n", Heading.class, SourceSpan.of(0, 2, 2, 3), SourceSpan.of(1, 2, 8, 3));
    }

    @Test
    public void indentedCodeBlock() {
        assertSpans("    foo\n", IndentedCodeBlock.class, SourceSpan.of(0, 0, 0, 7));
        assertSpans("     foo\n", IndentedCodeBlock.class, SourceSpan.of(0, 0, 0, 8));
        assertSpans("\tfoo\n", IndentedCodeBlock.class, SourceSpan.of(0, 0, 0, 4));
        assertSpans(" \tfoo\n", IndentedCodeBlock.class, SourceSpan.of(0, 0, 0, 5));
        assertSpans("  \tfoo\n", IndentedCodeBlock.class, SourceSpan.of(0, 0, 0, 6));
        assertSpans("   \tfoo\n", IndentedCodeBlock.class, SourceSpan.of(0, 0, 0, 7));
        assertSpans("    \tfoo\n", IndentedCodeBlock.class, SourceSpan.of(0, 0, 0, 8));
        assertSpans("    \t foo\n", IndentedCodeBlock.class, SourceSpan.of(0, 0, 0, 9));
        assertSpans("\t foo\n", IndentedCodeBlock.class, SourceSpan.of(0, 0, 0, 5));
        assertSpans("\t  foo\n", IndentedCodeBlock.class, SourceSpan.of(0, 0, 0, 6));
        assertSpans("    foo\n     bar\n", IndentedCodeBlock.class, SourceSpan.of(0, 0, 0, 7), SourceSpan.of(1, 0, 8, 8));
        assertSpans("    foo\n\tbar\n", IndentedCodeBlock.class, SourceSpan.of(0, 0, 0, 7), SourceSpan.of(1, 0, 8, 4));
        assertSpans("    foo\n    \n     \n", IndentedCodeBlock.class, SourceSpan.of(0, 0, 0, 7), SourceSpan.of(1, 0, 8, 4), SourceSpan.of(2, 0, 13, 5));
        assertSpans(">     foo\n", IndentedCodeBlock.class, SourceSpan.of(0, 2, 2, 7));
    }

    @Test
    public void fencedCodeBlock() {
        assertSpans("```\nfoo\n```\n", FencedCodeBlock.class,
                SourceSpan.of(0, 0, 0, 3), SourceSpan.of(1, 0, 4, 3), SourceSpan.of(2, 0, 8, 3));
        assertSpans("```\n foo\n```\n", FencedCodeBlock.class,
                SourceSpan.of(0, 0, 0, 3), SourceSpan.of(1, 0, 4, 4), SourceSpan.of(2, 0, 9, 3));
        assertSpans("```\nfoo\nbar\n```\n", FencedCodeBlock.class,
                SourceSpan.of(0, 0, 0, 3), SourceSpan.of(1, 0, 4, 3), SourceSpan.of(2, 0, 8, 3), SourceSpan.of(3, 0, 12, 3));
        assertSpans("   ```\n   foo\n   ```\n", FencedCodeBlock.class,
                SourceSpan.of(0, 0, 0, 6), SourceSpan.of(1, 0, 7, 6), SourceSpan.of(2, 0, 14, 6));
        assertSpans(" ```\n foo\nfoo\n```\n", FencedCodeBlock.class,
                SourceSpan.of(0, 0, 0, 4), SourceSpan.of(1, 0, 5, 4), SourceSpan.of(2, 0, 10, 3), SourceSpan.of(3, 0, 14, 3));
        assertSpans("```info\nfoo\n```\n", FencedCodeBlock.class,
                SourceSpan.of(0, 0, 0, 7), SourceSpan.of(1, 0, 8, 3), SourceSpan.of(2, 0, 12, 3));
        assertSpans("* ```\n  foo\n  ```\n", FencedCodeBlock.class,
                SourceSpan.of(0, 2, 2, 3), SourceSpan.of(1, 2, 8, 3), SourceSpan.of(2, 2, 14, 3));
        assertSpans("> ```\n> foo\n> ```\n", FencedCodeBlock.class,
                SourceSpan.of(0, 2, 2, 3), SourceSpan.of(1, 2, 8, 3), SourceSpan.of(2, 2, 14, 3));

        Node document = PARSER.parse("```\nfoo\n```\nbar\n");
        Paragraph paragraph = (Paragraph) document.getLastChild();
        assertEquals(List.of(SourceSpan.of(3, 0, 12, 3)), paragraph.getSourceSpans());
    }

    @Test
    public void htmlBlock() {
        assertSpans("<div>\n", HtmlBlock.class, SourceSpan.of(0, 0, 0, 5));
        assertSpans(" <div>\n foo\n </div>\n", HtmlBlock.class,
                SourceSpan.of(0, 0, 0, 6),
                SourceSpan.of(1, 0, 7, 4),
                SourceSpan.of(2, 0, 12, 7));
        assertSpans("* <div>\n", HtmlBlock.class, SourceSpan.of(0, 2, 2, 5));
    }

    @Test
    public void blockQuote() {
        assertSpans(">foo\n", BlockQuote.class, SourceSpan.of(0, 0, 0, 4));
        assertSpans("> foo\n", BlockQuote.class, SourceSpan.of(0, 0, 0, 5));
        assertSpans(">  foo\n", BlockQuote.class, SourceSpan.of(0, 0, 0, 6));
        assertSpans(" > foo\n", BlockQuote.class, SourceSpan.of(0, 0, 0, 6));
        assertSpans("   > foo\n  > bar\n", BlockQuote.class, SourceSpan.of(0, 0, 0, 8), SourceSpan.of(1, 0, 9, 7));
        // Lazy continuations
        assertSpans("> foo\nbar\n", BlockQuote.class, SourceSpan.of(0, 0, 0, 5), SourceSpan.of(1, 0, 6, 3));
        assertSpans("> foo\nbar\n> baz\n", BlockQuote.class, SourceSpan.of(0, 0, 0, 5), SourceSpan.of(1, 0, 6, 3), SourceSpan.of(2, 0, 10, 5));
        assertSpans("> > foo\nbar\n", BlockQuote.class, SourceSpan.of(0, 0, 0, 7), SourceSpan.of(1, 0, 8, 3));
    }

    @Test
    public void listBlock() {
        assertSpans("* foo\n", ListBlock.class, SourceSpan.of(0, 0, 0, 5));
        assertSpans("* foo\n  bar\n", ListBlock.class, SourceSpan.of(0, 0, 0, 5), SourceSpan.of(1, 0, 6, 5));
        assertSpans("* foo\n* bar\n", ListBlock.class, SourceSpan.of(0, 0, 0, 5), SourceSpan.of(1, 0, 6, 5));
        assertSpans("* foo\n  # bar\n", ListBlock.class, SourceSpan.of(0, 0, 0, 5), SourceSpan.of(1, 0, 6, 7));
        assertSpans("* foo\n  * bar\n", ListBlock.class, SourceSpan.of(0, 0, 0, 5), SourceSpan.of(1, 0, 6, 7));
        assertSpans("* foo\n> bar\n", ListBlock.class, SourceSpan.of(0, 0, 0, 5));
        assertSpans("> * foo\n", ListBlock.class, SourceSpan.of(0, 2, 2, 5));

        // Lazy continuations
        assertSpans("* foo\nbar\nbaz", ListBlock.class, SourceSpan.of(0, 0, 0, 5), SourceSpan.of(1, 0, 6, 3), SourceSpan.of(2, 0, 10, 3));
        assertSpans("* foo\nbar\n* baz", ListBlock.class, SourceSpan.of(0, 0, 0, 5), SourceSpan.of(1, 0, 6, 3), SourceSpan.of(2, 0, 10, 5));
        assertSpans("* foo\n  * bar\nbaz", ListBlock.class, SourceSpan.of(0, 0, 0, 5), SourceSpan.of(1, 0, 6, 7), SourceSpan.of(2, 0, 14, 3));

        Node document = PARSER.parse("* foo\n  * bar\n");
        ListBlock listBlock = (ListBlock) document.getFirstChild().getFirstChild().getLastChild();
        assertEquals(List.of(SourceSpan.of(1, 2, 8, 5)), listBlock.getSourceSpans());
    }

    @Test
    public void listItem() {
        assertSpans("* foo\n", ListItem.class, SourceSpan.of(0, 0, 0, 5));
        assertSpans(" * foo\n", ListItem.class, SourceSpan.of(0, 0, 0, 6));
        assertSpans("  * foo\n", ListItem.class, SourceSpan.of(0, 0, 0, 7));
        assertSpans("   * foo\n", ListItem.class, SourceSpan.of(0, 0, 0, 8));
        assertSpans("*\n  foo\n", ListItem.class, SourceSpan.of(0, 0, 0, 1), SourceSpan.of(1, 0, 2, 5));
        assertSpans("*\n  foo\n  bar\n", ListItem.class, SourceSpan.of(0, 0, 0, 1), SourceSpan.of(1, 0, 2, 5), SourceSpan.of(2, 0, 8, 5));
        assertSpans("> * foo\n", ListItem.class, SourceSpan.of(0, 2, 2, 5));

        // Lazy continuations
        assertSpans("* foo\nbar\n", ListItem.class, SourceSpan.of(0, 0, 0, 5), SourceSpan.of(1, 0, 6, 3));
        assertSpans("* foo\nbar\nbaz\n", ListItem.class, SourceSpan.of(0, 0, 0, 5), SourceSpan.of(1, 0, 6, 3), SourceSpan.of(2, 0, 10, 3));
    }

    @Test
    public void linkReferenceDefinition() {
        // This is tricky due to how link reference definition parsing works. It is stripped from the paragraph if it's
        // successfully parsed, otherwise it stays part of the paragraph.
        Node document = PARSER.parse("[foo]: /url\ntext\n");

        LinkReferenceDefinition linkReferenceDefinition = (LinkReferenceDefinition) document.getFirstChild();
        assertEquals(List.of(SourceSpan.of(0, 0, 0, 11)), linkReferenceDefinition.getSourceSpans());

        Paragraph paragraph = (Paragraph) document.getLastChild();
        assertEquals(List.of(SourceSpan.of(1, 0, 12, 4)), paragraph.getSourceSpans());
    }

    @Test
    public void linkReferenceDefinitionMultiple() {
        var doc = PARSER.parse("[foo]: /foo\n[bar]: /bar\n");
        var def1 = (LinkReferenceDefinition) doc.getFirstChild();
        var def2 = (LinkReferenceDefinition) doc.getLastChild();
        assertEquals(List.of(SourceSpan.of(0, 0, 0, 11)), def1.getSourceSpans());
        assertEquals(List.of(SourceSpan.of(1, 0, 12, 11)), def2.getSourceSpans());
    }

    @Test
    public void linkReferenceDefinitionWithTitle() {
        var doc = PARSER.parse("[1]: #not-code \"Text\"\n[foo]: /foo\n");
        var def1 = (LinkReferenceDefinition) doc.getFirstChild();
        var def2 = (LinkReferenceDefinition) doc.getLastChild();
        assertEquals(List.of(SourceSpan.of(0, 0, 0, 21)), def1.getSourceSpans());
        assertEquals(List.of(SourceSpan.of(1, 0, 22, 11)), def2.getSourceSpans());
    }

    @Test
    public void linkReferenceDefinitionWithTitleInvalid() {
        var doc = PARSER.parse("[foo]: /url\n\"title\" ok\n");
        var def = Nodes.find(doc, LinkReferenceDefinition.class);
        var paragraph = Nodes.find(doc, Paragraph.class);
        assertEquals(List.of(SourceSpan.of(0, 0, 0, 11)), def.getSourceSpans());
        assertEquals(List.of(SourceSpan.of(1, 0, 12, 10)), paragraph.getSourceSpans());
    }

    @Test
    public void linkReferenceDefinitionHeading() {
        // This is probably the trickiest because we have a link reference definition at the start of a paragraph
        // that gets replaced because of a heading. Phew.
        Node document = PARSER.parse("[foo]: /url\nHeading\n===\n");

        LinkReferenceDefinition linkReferenceDefinition = (LinkReferenceDefinition) document.getFirstChild();
        assertEquals(List.of(SourceSpan.of(0, 0, 0, 11)), linkReferenceDefinition.getSourceSpans());

        Heading heading = (Heading) document.getLastChild();
        assertEquals(List.of(SourceSpan.of(1, 0, 12, 7), SourceSpan.of(2, 0, 20, 3)), heading.getSourceSpans());
    }

    @Test
    public void lazyContinuationLines() {
        {
            // From https://spec.commonmark.org/0.31.2/#example-250
            // Wrong source span for the inner block quote for the second line.
            var doc = PARSER.parse("> > > foo\nbar\n");

            var bq1 = (BlockQuote) doc.getLastChild();
            assertEquals(List.of(SourceSpan.of(0, 0, 0, 9), SourceSpan.of(1, 0, 10, 3)), bq1.getSourceSpans());
            var bq2 = (BlockQuote) bq1.getLastChild();
            assertEquals(List.of(SourceSpan.of(0, 2, 2, 7), SourceSpan.of(1, 0, 10, 3)), bq2.getSourceSpans());
            var bq3 = (BlockQuote) bq2.getLastChild();
            assertEquals(List.of(SourceSpan.of(0, 4, 4, 5), SourceSpan.of(1, 0, 10, 3)), bq3.getSourceSpans());
            var paragraph = (Paragraph) bq3.getLastChild();
            assertEquals(List.of(SourceSpan.of(0, 6, 6, 3), SourceSpan.of(1, 0, 10, 3)), paragraph.getSourceSpans());
        }

        {
            // Adding one character to the last line remove blockQuote3 source for the second line
            var doc = PARSER.parse("> > > foo\nbars\n");

            var bq1 = (BlockQuote) doc.getLastChild();
            assertEquals(List.of(SourceSpan.of(0, 0, 0, 9), SourceSpan.of(1, 0, 10, 4)), bq1.getSourceSpans());
            var bq2 = (BlockQuote) bq1.getLastChild();
            assertEquals(List.of(SourceSpan.of(0, 2, 2, 7), SourceSpan.of(1, 0, 10, 4)), bq2.getSourceSpans());
            var bq3 = (BlockQuote) bq2.getLastChild();
            assertEquals(List.of(SourceSpan.of(0, 4, 4, 5), SourceSpan.of(1, 0, 10, 4)), bq3.getSourceSpans());
            var paragraph = (Paragraph) bq3.getLastChild();
            assertEquals(List.of(SourceSpan.of(0, 6, 6, 3), SourceSpan.of(1, 0, 10, 4)), paragraph.getSourceSpans());
        }

        {
            // From https://spec.commonmark.org/0.31.2/#example-292
            var doc = PARSER.parse("> 1. > Blockquote\ncontinued here.");

            var bq1 = (BlockQuote) doc.getLastChild();
            assertEquals(List.of(SourceSpan.of(0, 0, 0, 17), SourceSpan.of(1, 0, 18, 15)), bq1.getSourceSpans());
            var orderedList = (OrderedList) bq1.getLastChild();
            assertEquals(List.of(SourceSpan.of(0, 2, 2, 15), SourceSpan.of(1, 0, 18, 15)), orderedList.getSourceSpans());
            var listItem = (ListItem) orderedList.getLastChild();
            assertEquals(List.of(SourceSpan.of(0, 2, 2, 15), SourceSpan.of(1, 0, 18, 15)), listItem.getSourceSpans());
            var bq2 = (BlockQuote) listItem.getLastChild();
            assertEquals(List.of(SourceSpan.of(0, 5, 5, 12), SourceSpan.of(1, 0, 18, 15)), bq2.getSourceSpans());
            var paragraph = (Paragraph) bq2.getLastChild();
            assertEquals(List.of(SourceSpan.of(0, 7, 7, 10), SourceSpan.of(1, 0, 18, 15)), paragraph.getSourceSpans());
        }

        {
            // Lazy continuation line for nested blockquote
            var doc = PARSER.parse("> > foo\n> bar\n");

            var bq1 = (BlockQuote) doc.getLastChild();
            assertEquals(List.of(SourceSpan.of(0, 0, 0, 7), SourceSpan.of(1, 0, 8, 5)), bq1.getSourceSpans());
            var bq2 = (BlockQuote) bq1.getLastChild();
            assertEquals(List.of(SourceSpan.of(0, 2, 2, 5), SourceSpan.of(1, 2, 10, 3)), bq2.getSourceSpans());
            var paragraph = (Paragraph) bq2.getLastChild();
            assertEquals(List.of(SourceSpan.of(0, 4, 4, 3), SourceSpan.of(1, 2, 10, 3)), paragraph.getSourceSpans());
        }
    }

    @Test
    public void visualCheck() {
        assertVisualize("> * foo\n>   bar\n> * baz\n", "(> {[* <foo>]})\n(> {[  <bar>]})\n(> {⸢* ⸤baz⸥⸣})\n");
        assertVisualize("> * ```\n>   foo\n>   ```\n", "(> {[* <```>]})\n(> {[  <foo>]})\n(> {[  <```>]})\n");
    }

    @Test
    public void inlineText() {
        assertInlineSpans("foo", Text.class, SourceSpan.of(0, 0, 0, 3));
        assertInlineSpans("> foo", Text.class, SourceSpan.of(0, 2, 2, 3));
        assertInlineSpans("* foo", Text.class, SourceSpan.of(0, 2, 2, 3));

        // SourceSpans should be merged: ` is a separate Text node while inline parsing and gets merged at the end
        assertInlineSpans("foo`bar", Text.class, SourceSpan.of(0, 0, 0, 7));
        assertInlineSpans("foo[bar", Text.class, SourceSpan.of(0, 0, 0, 7));
        assertInlineSpans("> foo`bar", Text.class, SourceSpan.of(0, 2, 2, 7));

        assertInlineSpans("[foo](/url)", Text.class, SourceSpan.of(0, 1, 1, 3));
        assertInlineSpans("*foo*", Text.class, SourceSpan.of(0, 1, 1, 3));
    }

    @Test
    public void inlineHeading() {
        assertInlineSpans("# foo", Text.class, SourceSpan.of(0, 2, 2, 3));
        assertInlineSpans(" # foo", Text.class, SourceSpan.of(0, 3, 3, 3));
        assertInlineSpans("> # foo", Text.class, SourceSpan.of(0, 4, 4, 3));
    }

    @Test
    public void inlineAutolink() {
        assertInlineSpans("see <https://example.org>", Link.class, SourceSpan.of(0, 4, 4, 21));
    }

    @Test
    public void inlineBackslash() {
        assertInlineSpans("\\!", Text.class, SourceSpan.of(0, 0, 0, 2));
    }

    @Test
    public void inlineBackticks() {
        assertInlineSpans("see `code`", Code.class, SourceSpan.of(0, 4, 4, 6));
        assertInlineSpans("`multi\nline`", Code.class,
                SourceSpan.of(0, 0, 0, 6),
                SourceSpan.of(1, 0, 7, 5));
        assertInlineSpans("text ```", Text.class, SourceSpan.of(0, 0, 0, 8));
    }

    @Test
    public void inlineEntity() {
        assertInlineSpans("&amp;", Text.class, SourceSpan.of(0, 0, 0, 5));
    }

    @Test
    public void inlineHtml() {
        assertInlineSpans("hi <strong>there</strong>", HtmlInline.class, SourceSpan.of(0, 3, 3, 8));
    }

    @Test
    public void links() {
        assertInlineSpans("\n[text](/url)", Link.class, SourceSpan.of(1, 0, 1, 12));
        assertInlineSpans("\n[text](/url)", Text.class, SourceSpan.of(1, 1, 2, 4));

        assertInlineSpans("\n[text]\n\n[text]: /url", Link.class, SourceSpan.of(1, 0, 1, 6));
        assertInlineSpans("\n[text]\n\n[text]: /url", Text.class, SourceSpan.of(1, 1, 2, 4));
        assertInlineSpans("\n[text][]\n\n[text]: /url", Link.class, SourceSpan.of(1, 0, 1, 8));
        assertInlineSpans("\n[text][]\n\n[text]: /url", Text.class, SourceSpan.of(1, 1, 2, 4));
        assertInlineSpans("\n[text][ref]\n\n[ref]: /url", Link.class, SourceSpan.of(1, 0, 1, 11));
        assertInlineSpans("\n[text][ref]\n\n[ref]: /url", Text.class, SourceSpan.of(1, 1, 2, 4));
        assertInlineSpans("\n[notalink]", Text.class, SourceSpan.of(1, 0, 1, 10));
    }

    @Test
    public void inlineEmphasis() {
        assertInlineSpans("\n*hey*", Emphasis.class, SourceSpan.of(1, 0, 1, 5));
        assertInlineSpans("\n*hey*", Text.class, SourceSpan.of(1, 1, 2, 3));
        assertInlineSpans("\n**hey**", StrongEmphasis.class, SourceSpan.of(1, 0, 1, 7));
        assertInlineSpans("\n**hey**", Text.class, SourceSpan.of(1, 2, 3, 3));

        // This is an interesting one. It renders like this:
        // <p>*<em>hey</em></p>
        // The delimiter processor only uses one of the asterisks.
        // So the first Text node should be the `*` at the beginning with the correct span.
        assertInlineSpans("\n**hey*", Text.class, SourceSpan.of(1, 0, 1, 1));
        assertInlineSpans("\n**hey*", Emphasis.class, SourceSpan.of(1, 1, 2, 5));

        assertInlineSpans("\n***hey**", Text.class, SourceSpan.of(1, 0, 1, 1));
        assertInlineSpans("\n***hey**", StrongEmphasis.class, SourceSpan.of(1, 1, 2, 7));

        Node document = INLINES_PARSER.parse("*hey**");
        Node lastText = document.getFirstChild().getLastChild();
        assertEquals(List.of(SourceSpan.of(0, 5, 5, 1)), lastText.getSourceSpans());
    }

    @Test
    public void tabExpansion() {
        assertInlineSpans(">\tfoo", BlockQuote.class, SourceSpan.of(0, 0, 0, 5));
        assertInlineSpans(">\tfoo", Text.class, SourceSpan.of(0, 2, 2, 3));

        assertInlineSpans("a\tb", Text.class, SourceSpan.of(0, 0, 0, 3));
    }

    @Test
    public void differentLineTerminators() {
        var input = "foo\nbar\rbaz\r\nqux\r\n\r\n> *hi*";
        assertSpans(input, Paragraph.class,
                SourceSpan.of(0, 0, 0, 3),
                SourceSpan.of(1, 0, 4, 3),
                SourceSpan.of(2, 0, 8, 3),
                SourceSpan.of(3, 0, 13, 3));
        assertSpans(input, BlockQuote.class,
                SourceSpan.of(5, 0, 20, 6));

        assertInlineSpans(input, Emphasis.class, SourceSpan.of(5, 2, 22, 4));
    }

    private void assertVisualize(String source, String expected) {
        var doc = PARSER.parse(source);
        assertEquals(expected, SourceSpanRenderer.renderWithLineColumn(doc, source));
        assertEquals(expected, SourceSpanRenderer.renderWithInputIndex(doc, source));
    }

    private static void assertSpans(String input, Class<? extends Node> nodeClass, SourceSpan... expectedSourceSpans) {
        assertSpans(PARSER.parse(input), nodeClass, expectedSourceSpans);
        try {
            assertSpans(PARSER.parseReader(new StringReader(input)), nodeClass, expectedSourceSpans);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void assertInlineSpans(String input, Class<? extends Node> nodeClass, SourceSpan... expectedSourceSpans) {
        assertSpans(INLINES_PARSER.parse(input), nodeClass, expectedSourceSpans);
        try {
            assertSpans(INLINES_PARSER.parseReader(new StringReader(input)), nodeClass, expectedSourceSpans);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void assertSpans(Node rootNode, Class<? extends Node> nodeClass, SourceSpan... expectedSourceSpans) {
        Node node = findNode(rootNode, nodeClass);
        assertEquals(List.of(expectedSourceSpans), node.getSourceSpans());
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
