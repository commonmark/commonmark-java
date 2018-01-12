package org.commonmark.test;

import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Stopwatch;
import org.junit.rules.Timeout;
import org.junit.runner.Description;
import org.junit.runners.MethodSorters;

import java.util.concurrent.TimeUnit;

import static org.commonmark.testutil.Strings.repeat;

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
                repeat("*a **a ", x) + "b" + repeat(" a** a*", x),
                "<p>" + repeat("<em>a <strong>a ", x) + "b" +
                        repeat(" a</strong> a</em>", x) + "</p>\n");
    }

    @Test
    public void emphasisClosersWithNoOpeners() {
        assertRendering(
                repeat("a_ ", x),
                "<p>" + repeat("a_ ", x - 1) + "a_</p>\n");
    }

    @Test
    public void emphasisOpenersWithNoClosers() {
        assertRendering(
                repeat("_a ", x),
                "<p>" + repeat("_a ", x - 1) + "_a</p>\n");
    }

    @Test
    public void linkClosersWithNoOpeners() {
        assertRendering(
                repeat("a] ", x),
                "<p>" + repeat("a] ", x - 1) + "a]</p>\n");
    }

    @Test
    public void linkOpenersWithNoClosers() {
        assertRendering(
                repeat("[a ", x),
                "<p>" + repeat("[a ", x - 1) + "[a</p>\n");
    }

    @Test
    public void linkOpenersAndEmphasisClosers() {
        assertRendering(
                repeat("[ a_ ", x),
                "<p>" + repeat("[ a_ ", x - 1) + "[ a_</p>\n");
    }

    @Test
    public void mismatchedOpenersAndClosers() {
        assertRendering(
                repeat("*a_ ", x),
                "<p>" + repeat("*a_ ", x - 1) + "*a_</p>\n");
    }

    @Test
    public void nestedBrackets() {
        assertRendering(
                repeat("[", x) + "a" + repeat("]", x),
                "<p>" + repeat("[", x) + "a" + repeat("]", x) + "</p>\n");
    }

    @Test
    public void nestedBlockQuotes() {
        // this is limited by the stack size because visitor is recursive
        x = 1000;
        assertRendering(
                repeat("> ", x) + "a\n",
                repeat("<blockquote>\n", x) + "<p>a</p>\n" +
                        repeat("</blockquote>\n", x));
    }
}
