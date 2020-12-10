package org.commonmark.internal.inline;

import org.commonmark.node.SourceSpan;
import org.commonmark.parser.SourceLine;
import org.commonmark.parser.SourceLines;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class ScannerTest {

    @Test
    public void testNext() {
        Scanner scanner = new Scanner(Collections.singletonList(
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
        Scanner scanner = new Scanner(Arrays.asList(
                SourceLine.of("ab", null),
                SourceLine.of("cde", null)),
                0, 0);
        assertTrue(scanner.hasNext());
        assertEquals('\0', scanner.peekPrevious());
        assertEquals('a', scanner.peek());
        scanner.next();

        assertTrue(scanner.hasNext());
        assertEquals('a', scanner.peekPrevious());
        assertEquals('b', scanner.peek());
        scanner.next();

        assertTrue(scanner.hasNext());
        assertEquals('b', scanner.peekPrevious());
        assertEquals('\n', scanner.peek());
        scanner.next();

        assertTrue(scanner.hasNext());
        assertEquals('\n', scanner.peekPrevious());
        assertEquals('c', scanner.peek());
        scanner.next();

        assertTrue(scanner.hasNext());
        assertEquals('c', scanner.peekPrevious());
        assertEquals('d', scanner.peek());
        scanner.next();

        assertTrue(scanner.hasNext());
        assertEquals('d', scanner.peekPrevious());
        assertEquals('e', scanner.peek());
        scanner.next();

        assertFalse(scanner.hasNext());
        assertEquals('e', scanner.peekPrevious());
        assertEquals('\0', scanner.peek());
    }


    @Test
    public void testTextBetween() {
        Scanner scanner = new Scanner(Arrays.asList(
                SourceLine.of("ab", SourceSpan.of(10, 3, 2)),
                SourceLine.of("cde", SourceSpan.of(11, 4, 3))),
                0, 0);

        Position start = scanner.position();

        scanner.next();
        assertSourceLines(scanner.getSource(start, scanner.position()),
                "a",
                SourceSpan.of(10, 3, 1));

        Position afterA = scanner.position();

        scanner.next();
        assertSourceLines(scanner.getSource(start, scanner.position()),
                "ab",
                SourceSpan.of(10, 3, 2));

        Position afterB = scanner.position();

        scanner.next();
        assertSourceLines(scanner.getSource(start, scanner.position()),
                "ab\n",
                SourceSpan.of(10, 3, 2));

        scanner.next();
        assertSourceLines(scanner.getSource(start, scanner.position()),
                "ab\nc",
                SourceSpan.of(10, 3, 2),
                SourceSpan.of(11, 4, 1));

        scanner.next();
        assertSourceLines(scanner.getSource(start, scanner.position()),
                "ab\ncd",
                SourceSpan.of(10, 3, 2),
                SourceSpan.of(11, 4, 2));

        scanner.next();
        assertSourceLines(scanner.getSource(start, scanner.position()),
                "ab\ncde",
                SourceSpan.of(10, 3, 2),
                SourceSpan.of(11, 4, 3));

        assertSourceLines(scanner.getSource(afterA, scanner.position()),
                "b\ncde",
                SourceSpan.of(10, 4, 1),
                SourceSpan.of(11, 4, 3));

        assertSourceLines(scanner.getSource(afterB, scanner.position()),
                "\ncde",
                SourceSpan.of(11, 4, 3));
    }

    private void assertSourceLines(SourceLines sourceLines, String expectedContent, SourceSpan... expectedSourceSpans) {
        assertEquals(expectedContent, sourceLines.getContent());
        assertEquals(Arrays.asList(expectedSourceSpans), sourceLines.getSourceSpans());
    }

    @Test
    public void nextString() {
        Scanner scanner = Scanner.of(SourceLines.of(Arrays.asList(
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
