package org.commonmark.test;

import org.commonmark.testutil.Strings;
import org.junit.Test;

public class SpecialInputTest extends CoreRenderingTestCase {

    @Test
    public void empty() {
        assertRendering("", "");
    }

    @Test
    public void nullCharacterShouldBeReplaced() {
        assertRendering("foo\0bar", "<p>foo\uFFFDbar</p>\n");
    }

    @Test
    public void nullCharacterEntityShouldBeReplaced() {
        assertRendering("foo&#0;bar", "<p>foo\uFFFDbar</p>\n");
    }

    @Test
    public void crLfAsLineSeparatorShouldBeParsed() {
        assertRendering("foo\r\nbar", "<p>foo\nbar</p>\n");
    }

    @Test
    public void crLfAtEndShouldBeParsed() {
        assertRendering("foo\r\n", "<p>foo</p>\n");
    }

    @Test
    public void mixedLineSeparators() {
        assertRendering("- a\n- b\r- c\r\n- d", "<ul>\n<li>a</li>\n<li>b</li>\n<li>c</li>\n<li>d</li>\n</ul>\n");
        assertRendering("a\n\nb\r\rc\r\n\r\nd\n\re", "<p>a</p>\n<p>b</p>\n<p>c</p>\n<p>d</p>\n<p>e</p>\n");
    }

    @Test
    public void surrogatePair() {
        assertRendering("surrogate pair: \uD834\uDD1E", "<p>surrogate pair: \uD834\uDD1E</p>\n");
    }

    @Test
    public void surrogatePairInLinkDestination() {
        assertRendering("[title](\uD834\uDD1E)", "<p><a href=\"\uD834\uDD1E\">title</a></p>\n");
    }

    @Test
    public void indentedCodeBlockWithMixedTabsAndSpaces() {
        assertRendering("    foo\n\tbar", "<pre><code>foo\nbar\n</code></pre>\n");
    }

    @Test
    public void tightListInBlockQuote() {
        assertRendering("> *\n> * a", "<blockquote>\n<ul>\n<li></li>\n<li>a</li>\n</ul>\n</blockquote>\n");
    }

    @Test
    public void looseListInBlockQuote() {
        // Second line in block quote is considered blank for purpose of loose list
        assertRendering("> *\n>\n> * a", "<blockquote>\n<ul>\n<li></li>\n<li>\n<p>a</p>\n</li>\n</ul>\n</blockquote>\n");
    }

    @Test
    public void lineWithOnlySpacesAfterListBullet() {
        assertRendering("-  \n  \n  foo\n", "<ul>\n<li></li>\n</ul>\n<p>foo</p>\n");
    }

    @Test
    public void listWithTwoSpacesForFirstBullet() {
        // We have two spaces after the bullet, but no content. With content, the next line would be required
        assertRendering("*  \n  foo\n", "<ul>\n<li>foo</li>\n</ul>\n");
    }

    @Test
    public void orderedListMarkerOnly() {
        assertRendering("2.", "<ol start=\"2\">\n<li></li>\n</ol>\n");
    }

    @Test
    public void columnIsInTabOnPreviousLine() {
        assertRendering("- foo\n\n\tbar\n\n# baz\n",
                "<ul>\n<li>\n<p>foo</p>\n<p>bar</p>\n</li>\n</ul>\n<h1>baz</h1>\n");
        assertRendering("- foo\n\n\tbar\n# baz\n",
                "<ul>\n<li>\n<p>foo</p>\n<p>bar</p>\n</li>\n</ul>\n<h1>baz</h1>\n");
    }

    @Test
    public void linkLabelWithBracket() {
        assertRendering("[a[b]\n\n[a[b]: /", "<p>[a[b]</p>\n<p>[a[b]: /</p>\n");
        assertRendering("[a]b]\n\n[a]b]: /", "<p>[a]b]</p>\n<p>[a]b]: /</p>\n");
        assertRendering("[a[b]]\n\n[a[b]]: /", "<p>[a[b]]</p>\n<p>[a[b]]: /</p>\n");
    }

    @Test
    public void linkLabelLength() {
        String label1 = Strings.repeat("a", 999);
        assertRendering("[foo][" + label1 + "]\n\n[" + label1 + "]: /", "<p><a href=\"/\">foo</a></p>\n");
        assertRendering("[foo][x" + label1 + "]\n\n[x" + label1 + "]: /",
                "<p>[foo][x" + label1 + "]</p>\n<p>[x" + label1 + "]: /</p>\n");
        assertRendering("[foo][\n" + label1 + "]\n\n[\n" + label1 + "]: /",
                "<p>[foo][\n" + label1 + "]</p>\n<p>[\n" + label1 + "]: /</p>\n");

        String label2 = Strings.repeat("a\n", 499);
        assertRendering("[foo][" + label2 + "]\n\n[" + label2 + "]: /", "<p><a href=\"/\">foo</a></p>\n");
        assertRendering("[foo][12" + label2 + "]\n\n[12" + label2 + "]: /",
                "<p>[foo][12" + label2 + "]</p>\n<p>[12" + label2 + "]: /</p>\n");
    }

    @Test
    public void linkDestinationEscaping() {
        // Backslash escapes `)`
        assertRendering("[foo](\\))", "<p><a href=\")\">foo</a></p>\n");
        // ` ` is not escapable, so the backslash is a literal backslash and there's an optional space at the end
        assertRendering("[foo](\\ )", "<p><a href=\"\\\">foo</a></p>\n");
        // Backslash escapes `>`, so it's not a `(<...>)` link, but a `(...)` link instead
        assertRendering("[foo](<\\>)", "<p><a href=\"&lt;&gt;\">foo</a></p>\n");
        // Backslash is a literal, so valid
        assertRendering("[foo](<a\\b>)", "<p><a href=\"a\\b\">foo</a></p>\n");
        // Backslash escapes `>` but there's another `>`, valid
        assertRendering("[foo](<a\\>>)", "<p><a href=\"a&gt;\">foo</a></p>\n");
    }

    // commonmark/CommonMark#468
    @Test
    public void linkReferenceBackslash() {
        // Backslash escapes ']', so not a valid link label
        assertRendering("[\\]: test", "<p>[]: test</p>\n");
        // Backslash is a literal, so valid
        assertRendering("[a\\b]\n\n[a\\b]: test", "<p><a href=\"test\">a\\b</a></p>\n");
        // Backslash escapes `]` but there's another `]`, valid
        assertRendering("[a\\]]\n\n[a\\]]: test", "<p><a href=\"test\">a]</a></p>\n");
    }

    // commonmark/cmark#177
    @Test
    public void emphasisMultipleOf3Rule() {
        assertRendering("a***b* c*", "<p>a*<em><em>b</em> c</em></p>\n");
    }
}
