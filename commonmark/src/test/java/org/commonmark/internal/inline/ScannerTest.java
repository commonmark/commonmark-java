package org.commonmark.internal.inline;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ScannerTest {
    
    @Test
    public void testNext() {
        Scanner scanner = new Scanner("foo bar", 4);
        assertEquals('b', scanner.peek());
        scanner.next();
        assertEquals('a', scanner.peek());
        scanner.next();
        assertEquals('r', scanner.peek());
        scanner.next();
        assertEquals('\0', scanner.peek());
    }
}
