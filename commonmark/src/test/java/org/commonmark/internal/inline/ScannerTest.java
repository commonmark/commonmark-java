package org.commonmark.internal.inline;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class ScannerTest {

    @Test
    public void testNext() {
        Scanner scanner = new Scanner(Collections.<CharSequence>singletonList("foo bar"), 0, 4);
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
        Scanner scanner = new Scanner(Arrays.<CharSequence>asList("ab", "cde"), 0, 0);
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
        Scanner scanner = new Scanner(Arrays.<CharSequence>asList("ab", "cde"), 0, 0);
        Position start = scanner.position();
        scanner.next();
        assertEquals("a", scanner.textBetween(start, scanner.position()));
        Position afterA = scanner.position();
        scanner.next();
        assertEquals("ab", scanner.textBetween(start, scanner.position()));
        scanner.next();
        assertEquals("ab\n", scanner.textBetween(start, scanner.position()));
        scanner.next();
        assertEquals("ab\nc", scanner.textBetween(start, scanner.position()));
        scanner.next();
        assertEquals("ab\ncd", scanner.textBetween(start, scanner.position()));
        scanner.next();
        assertEquals("ab\ncde", scanner.textBetween(start, scanner.position()));

        assertEquals("b\ncde", scanner.textBetween(afterA, scanner.position()));
    }

    @Test
    public void nextString() {
        Scanner scanner = Scanner.of(Arrays.<CharSequence>asList("hey ya", "hi"));
        assertFalse(scanner.next("hoy"));
        assertTrue(scanner.next("hey"));
        assertTrue(scanner.next(' '));
        assertFalse(scanner.next("yo"));
        assertTrue(scanner.next("ya"));
        assertFalse(scanner.next(" "));
    }
}
