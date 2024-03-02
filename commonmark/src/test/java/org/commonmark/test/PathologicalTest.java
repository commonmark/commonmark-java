package org.commonmark.test;

import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Stopwatch;
import org.junit.rules.Timeout;
import org.junit.runner.Description;
import org.junit.runners.MethodSorters;

import java.util.concurrent.TimeUnit;

/**
 * Pathological input cases (from commonmark.js).
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PathologicalTest extends CoreRenderingTestCase {

    private int x = 100_000;

    @Rule
    public Timeout timeout = new Timeout(3, TimeUnit.SECONDS);

    @Rule
    public Stopwatch stopwatch = new Stopwatch() {
        @Override
        protected void finished(long nanos, Description description) {
            System.err.println(description.getDisplayName() + " took " + (nanos / 1000000) + " ms");
        }
    };

    @Test
    public void nestedStrongEmphasis() {
        // this is limited by the stack size because visitor is recursive
        x = 500;
        assertRendering(
                "*a **a ".repeat(x) + "b" + " a** a*".repeat(x),
                "<p>" + "<em>a <strong>a ".repeat(x) + "b" +
                        " a</strong> a</em>".repeat(x) + "</p>\n");
    }

    @Test
    public void emphasisClosersWithNoOpeners() {
        assertRendering(
                "a_ ".repeat(x),
                "<p>" + "a_ ".repeat(x - 1) + "a_</p>\n");
    }

    @Test
    public void emphasisOpenersWithNoClosers() {
        assertRendering(
                "_a ".repeat(x),
                "<p>" + "_a ".repeat(x - 1) + "_a</p>\n");
    }

    @Test
    public void linkClosersWithNoOpeners() {
        assertRendering(
                "a] ".repeat(x),
                "<p>" + "a] ".repeat(x - 1) + "a]</p>\n");
    }

    @Test
    public void linkOpenersWithNoClosers() {
        assertRendering(
                "[a ".repeat(x),
                "<p>" + "[a ".repeat(x - 1) + "[a</p>\n");
    }

    @Test
    public void linkOpenersAndEmphasisClosers() {
        assertRendering(
                "[ a_ ".repeat(x),
                "<p>" + "[ a_ ".repeat(x - 1) + "[ a_</p>\n");
    }

    @Test
    public void mismatchedOpenersAndClosers() {
        assertRendering(
                "*a_ ".repeat(x),
                "<p>" + "*a_ ".repeat(x - 1) + "*a_</p>\n");
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
        assertRendering(
                "> ".repeat(x) + "a\n",
                "<blockquote>\n".repeat(x) + "<p>a</p>\n" +
                        "</blockquote>\n".repeat(x));
    }

    @Test
    public void hugeHorizontalRule() {
        assertRendering(
                "*".repeat(10000) + "\n",
                "<hr />\n");
    }

    @Test
    public void backslashInLink() {
        // See https://github.com/commonmark/commonmark.js/issues/157
        assertRendering("[" + "\\".repeat(x) + "\n",
                "<p>" + "[" + "\\".repeat(x / 2) + "</p>\n");
    }

    @Test
    public void unclosedInlineLinks() {
        // See https://github.com/commonmark/commonmark.js/issues/129
        assertRendering("[](".repeat(x) + "\n",
                "<p>" + "[](".repeat(x) + "</p>\n");
    }
}
