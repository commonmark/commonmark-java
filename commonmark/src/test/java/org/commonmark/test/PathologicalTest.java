package org.commonmark.test;

import java.util.concurrent.TimeUnit;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.testutil.Asserts;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Timeout;

/** Pathological input cases (from commonmark.js). */
@Timeout(value = 3, unit = TimeUnit.SECONDS)
@TestMethodOrder(MethodOrderer.MethodName.class)
public class PathologicalTest extends CoreRenderingTestCase {

    private int x = 100_000;

    @Test
    public void nestedStrongEmphasis() {
        // this is limited by the stack size because visitor is recursive
        x = 500;
        assertRendering(
                "*a **a ".repeat(x) + "b" + " a** a*".repeat(x),
                "<p>"
                        + "<em>a <strong>a ".repeat(x)
                        + "b"
                        + " a</strong> a</em>".repeat(x)
                        + "</p>\n");
    }

    @Test
    public void emphasisClosersWithNoOpeners() {
        assertRendering("a_ ".repeat(x), "<p>" + "a_ ".repeat(x - 1) + "a_</p>\n");
    }

    @Test
    public void emphasisOpenersWithNoClosers() {
        assertRendering("_a ".repeat(x), "<p>" + "_a ".repeat(x - 1) + "_a</p>\n");
    }

    @Test
    public void linkClosersWithNoOpeners() {
        assertRendering("a] ".repeat(x), "<p>" + "a] ".repeat(x - 1) + "a]</p>\n");
    }

    @Test
    public void linkOpenersWithNoClosers() {
        assertRendering("[a ".repeat(x), "<p>" + "[a ".repeat(x - 1) + "[a</p>\n");
    }

    @Test
    public void linkOpenersAndEmphasisClosers() {
        assertRendering("[ a_ ".repeat(x), "<p>" + "[ a_ ".repeat(x - 1) + "[ a_</p>\n");
    }

    @Test
    public void mismatchedOpenersAndClosers() {
        assertRendering("*a_ ".repeat(x), "<p>" + "*a_ ".repeat(x - 1) + "*a_</p>\n");
    }

    @Test
    public void nestedBrackets() {
        assertRendering(
                "[".repeat(x) + "a" + "]".repeat(x),
                "<p>" + "[".repeat(x) + "a" + "]".repeat(x) + "</p>\n");
    }

    @Test
    public void nestedBlockQuotes() {
        // this is limited by the stack size because visitor is recursive
        x = 1000;
        var source = "> ".repeat(x) + "a\n";
        var expected = "<blockquote>\n".repeat(x) + "<p>a</p>\n" + "</blockquote>\n".repeat(x);

        var parser = Parser.builder().maxOpenBlockParsers(Integer.MAX_VALUE).build();
        var renderer = HtmlRenderer.builder().build();
        Asserts.assertRendering(source, expected, renderer.render(parser.parse(source)));
    }

    @Test
    public void hugeHorizontalRule() {
        assertRendering("*".repeat(10000) + "\n", "<hr />\n");
    }

    @Test
    public void backslashInLink() {
        // See https://github.com/commonmark/commonmark.js/issues/157
        assertRendering("[" + "\\".repeat(x) + "\n", "<p>" + "[" + "\\".repeat(x / 2) + "</p>\n");
    }

    @Test
    public void unclosedInlineLinks() {
        // See https://github.com/commonmark/commonmark.js/issues/129
        assertRendering("[](".repeat(x) + "\n", "<p>" + "[](".repeat(x) + "</p>\n");
    }

    // The following cases each start an inline HTML construct whose terminator never occurs, so
    // every occurrence used to scan the rest of the input again. They all contain a `>` close to
    // each `<` so that the separate autolink scan for `>` stays cheap and only the inline HTML
    // scanning is measured. The leading text keeps the line from starting with `<`, which would
    // parse as an HTML block instead.

    @Test
    public void htmlProcessingInstructionsWithNoEnd() {
        // `?>` never occurs because of the space
        assertRendering("x <? >".repeat(x), "<p>" + "x &lt;? &gt;".repeat(x) + "</p>\n");
    }

    @Test
    public void htmlCommentsWithNoEnd() {
        // `-->` never occurs because of the space
        assertRendering("x <!-- >".repeat(x), "<p>" + "x &lt;!-- &gt;".repeat(x) + "</p>\n");
    }

    @Test
    public void htmlCdataWithNoEnd() {
        // `]]>` never occurs, there's no `]` at all
        assertRendering(
                "x <![CDATA[ >".repeat(x), "<p>" + "x &lt;![CDATA[ &gt;".repeat(x) + "</p>\n");
    }
}
