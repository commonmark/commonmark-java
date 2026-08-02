package org.commonmark.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayDeque;
import java.util.concurrent.TimeUnit;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.testutil.Asserts;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Timeout;

/** Pathological input cases. */
@Timeout(value = 3, unit = TimeUnit.SECONDS)
@TestMethodOrder(MethodOrderer.MethodName.class)
public class PathologicalTest extends CoreRenderingTestCase {

    private int x = 100_000;

    @Test
    public void nestedStrongEmphasis() {
        // This is about parsing/rendering capacity, not about maxInlineNesting (which
        // defaults to 100 and would otherwise cap this at a much shallower depth even though
        // every level here is a distinct, unambiguous emphasis/strong pair) -- so the limit is
        // disabled and depth is instead limited by the stack size, because the visitor is
        // recursive.
        x = 500;
        var source = "*a **a ".repeat(x) + "b" + " a** a*".repeat(x);
        var expected =
                "<p>"
                        + "<em>a <strong>a ".repeat(x)
                        + "b"
                        + " a</strong> a</em>".repeat(x)
                        + "</p>\n";

        var parser = Parser.builder().maxInlineNesting(Integer.MAX_VALUE).build();
        var renderer = HtmlRenderer.builder().build();
        Asserts.assertRendering(source, expected, renderer.render(parser.parse(source)));
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
    public void openerSearchLowerBound() {
        var s = "a**b" + "c* ".repeat(80_000); // ~240 KB
        assertRendering(s, "<p>" + s.trim() + "</p>\n");
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

    @Test
    public void autolinkWithoutCloser() {
        var count = 160_000;
        // 160 KB, no '>'
        var source = "<".repeat(count);
        assertRendering(source, "<p>" + "&lt;".repeat(count) + "</p>\n");
    }

    @Test
    public void backticksDistinctLengths() {
        var sb = new StringBuilder();
        // ~4.5 MB
        for (int i = 1; i <= 3000; i++) {
            sb.append("`".repeat(i));
            sb.append('x');
        }
        var source = sb.toString();
        assertRendering(source, "<p>" + source + "</p>\n");
    }

    @Test
    public void htmlBlockAttributes() {
        // ~8 KB: "<a a a a ..." (no closing '>')
        var s = "<a" + " a".repeat(4000);
        assertRendering(s, "<p>&lt;a" + " a".repeat(4000) + "</p>\n");
    }

    @Test
    public void autolinkEmail() {
        // ~8 KB autolink
        var email = "a@" + "a.".repeat(4000) + "a";
        var s = "<" + email + ">";
        assertRendering(s, "<p><a href=\"mailto:" + email + "\">" + email + "</a></p>\n");
    }

    // The following cases all nest inline nodes one level deeper for a small, constant amount of
    // input, which would break the recursive renderer. They differ in how the nesting comes about,
    // and are all limited by maxInlineNesting (which defaults to 100).

    @Test
    public void nestedEmphasisSamePair() {
        // The same pair of runs is matched over and over, two characters at a time.
        int n = 40_000;
        assertNestingLimited("*".repeat(n) + "x" + "*".repeat(n));
    }

    @Test
    public void nestedEmphasisReusedOpener() {
        // One long run is matched against a series of single-character closers, each of which is
        // used up immediately, so only the opener is reused.
        int n = 40_000;
        assertNestingLimited("*".repeat(n) + "x" + "*".repeat(n) + "b*".repeat(n));
    }

    @Test
    public void nestedEmphasisDifferentDelimiters() {
        // Every level uses a different pair of runs, so no single delimiter is reused.
        int n = 40_000;
        assertNestingLimited("*a ".repeat(n) + " b*".repeat(n));
    }

    @Test
    public void nestedImages() {
        // Images can contain images (unlike links, which can't contain links).
        int n = 40_000;
        assertNestingLimited("![".repeat(n) + "x" + "](u)".repeat(n));
    }

    @Test
    public void nestedEmphasisAndImagesInterleaved() {
        // Alternating between the two, to check that neither can be used to hide nesting from the
        // limit of the other.
        int n = 20_000;
        assertNestingLimited("*![".repeat(n) + "x" + "](u)*".repeat(n));
        assertNestingLimited("![*".repeat(n) + "x" + "*](u)".repeat(n));
    }

    /**
     * Assert that the source can be parsed and rendered without a {@link StackOverflowError}, and
     * that the resulting tree is not nested much deeper than the default {@code maxInlineNesting}
     * of 100 (a few levels of slack for the block nodes and for a limit being applied one level
     * late).
     */
    private void assertNestingLimited(String source) {
        var document = Parser.builder().build().parse(source);

        assertThat(nesting(document)).isLessThan(110);

        // Doesn't throw StackOverflowError (the renderer is recursive).
        HtmlRenderer.builder().build().render(document);
    }

    /** The depth of the deepest node, determined without recursion. */
    private int nesting(Node document) {
        var nodes = new ArrayDeque<Node>();
        var depths = new ArrayDeque<Integer>();
        nodes.add(document);
        depths.add(0);

        var max = 0;
        while (!nodes.isEmpty()) {
            var node = nodes.removeLast();
            var depth = depths.removeLast();
            max = Math.max(max, depth);
            for (var child = node.getFirstChild(); child != null; child = child.getNext()) {
                nodes.addLast(child);
                depths.addLast(depth + 1);
            }
        }
        return max;
    }
}
