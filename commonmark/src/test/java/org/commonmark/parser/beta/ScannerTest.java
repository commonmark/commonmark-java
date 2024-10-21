package org.commonmark.parser.beta;

import org.commonmark.node.SourceSpan;
import org.commonmark.parser.SourceLine;
import org.commonmark.parser.SourceLines;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ScannerTest {

    @Test
    public void testNext() {
        Scanner scanner = new Scanner(List.of(
                SourceLine.of("foo bar", null)),
                0, 4);
        assertEquals('b', scanner.peek());
        scanner.next();
        assertEquals('a', scanner.peek());
        scanner.next();
        assertEquals('r', scanner.peek());
        scanner.next();
        assertEquals('\0', scanner.peek());
    }

    @Test
    public void testMultipleLines() {
        Scanner scanner = new Scanner(List.of(
                SourceLine.of("ab", null),
                SourceLine.of("cde", null)),
                0, 0);
        assertTrue(scanner.hasNext());
        assertEquals('\0', scanner.peekPreviousCodePoint());
        assertEquals('a', scanner.peek());
        scanner.next();

        assertTrue(scanner.hasNext());
        assertEquals('a', scanner.peekPreviousCodePoint());
        assertEquals('b', scanner.peek());
        scanner.next();

        assertTrue(scanner.hasNext());
        assertEquals('b', scanner.peekPreviousCodePoint());
        assertEquals('\n', scanner.peek());
        scanner.next();

        assertTrue(scanner.hasNext());
        assertEquals('\n', scanner.peekPreviousCodePoint());
        assertEquals('c', scanner.peek());
        scanner.next();

        assertTrue(scanner.hasNext());
        assertEquals('c', scanner.peekPreviousCodePoint());
        assertEquals('d', scanner.peek());
        scanner.next();

        assertTrue(scanner.hasNext());
        assertEquals('d', scanner.peekPreviousCodePoint());
        assertEquals('e', scanner.peek());
        scanner.next();

        assertFalse(scanner.hasNext());
        assertEquals('e', scanner.peekPreviousCodePoint());
        assertEquals('\0', scanner.peek());
    }

    @Test
    public void testCodePoints() {
        Scanner scanner = new Scanner(List.of(SourceLine.of("\uD83D\uDE0A", null)), 0, 0);

        assertTrue(scanner.hasNext());
        assertEquals('\0', scanner.peekPreviousCodePoint());
        assertEquals(128522, scanner.peekCodePoint());
        scanner.next();
        // This jumps chars, not code points. So jump two here
        scanner.next();

        assertFalse(scanner.hasNext());
        assertEquals(128522, scanner.peekPreviousCodePoint());
        assertEquals('\0', scanner.peekCodePoint());
    }

    @Test
    public void testTextBetween() {
        Scanner scanner = new Scanner(List.of(
                SourceLine.of("ab", SourceSpan.of(10, 3, 13, 2)),
                SourceLine.of("cde", SourceSpan.of(11, 4, 20, 3))),
                0, 0);

        Position start = scanner.position();

        scanner.next();
        assertSourceLines(scanner.getSource(start, scanner.position()),
                "a",
                SourceSpan.of(10, 3, 13, 1));

        Position afterA = scanner.position();

        scanner.next();
        assertSourceLines(scanner.getSource(start, scanner.position()),
                "ab",
                SourceSpan.of(10, 3, 13, 2));

        Position afterB = scanner.position();

        scanner.next();
        assertSourceLines(scanner.getSource(start, scanner.position()),
                "ab\n",
                SourceSpan.of(10, 3, 13, 2));

        scanner.next();
        assertSourceLines(scanner.getSource(start, scanner.position()),
                "ab\nc",
                SourceSpan.of(10, 3, 13, 2),
                SourceSpan.of(11, 4, 20, 1));

        scanner.next();
        assertSourceLines(scanner.getSource(start, scanner.position()),
                "ab\ncd",
                SourceSpan.of(10, 3, 13, 2),
                SourceSpan.of(11, 4, 20, 2));

        scanner.next();
        assertSourceLines(scanner.getSource(start, scanner.position()),
                "ab\ncde",
                SourceSpan.of(10, 3, 13, 2),
                SourceSpan.of(11, 4, 20, 3));

        assertSourceLines(scanner.getSource(afterA, scanner.position()),
                "b\ncde",
                SourceSpan.of(10, 4, 14, 1),
                SourceSpan.of(11, 4, 20, 3));

        assertSourceLines(scanner.getSource(afterB, scanner.position()),
                "\ncde",
                SourceSpan.of(11, 4, 20, 3));
    }

    private void assertSourceLines(SourceLines sourceLines, String expectedContent, SourceSpan... expectedSourceSpans) {
        assertEquals(expectedContent, sourceLines.getContent());
        assertEquals(List.of(expectedSourceSpans), sourceLines.getSourceSpans());
    }

    @Test
    public void nextString() {
        Scanner scanner = Scanner.of(SourceLines.of(List.of(
                SourceLine.of("hey ya", null),
                SourceLine.of("hi", null))));
        assertFalse(scanner.next("hoy"));
        assertTrue(scanner.next("hey"));
        assertTrue(scanner.next(' '));
        assertFalse(scanner.next("yo"));
        assertTrue(scanner.next("ya"));
        assertFalse(scanner.next(" "));
    }
}
