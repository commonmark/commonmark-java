package org.commonmark.test;

import org.commonmark.node.SourceSpan;
import org.commonmark.parser.SourceLine;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SourceLineTest {

    @Test
    public void testSubstring() {
        SourceLine line = SourceLine.of("abcd", SourceSpan.of(3, 10, 13, 4));

        assertSourceLine(line.substring(0, 4), "abcd", SourceSpan.of(3, 10, 13, 4));
        assertSourceLine(line.substring(0, 3), "abc", SourceSpan.of(3, 10, 13, 3));
        assertSourceLine(line.substring(0, 2), "ab", SourceSpan.of(3, 10, 13, 2));
        assertSourceLine(line.substring(0, 1), "a", SourceSpan.of(3, 10, 13, 1));
        assertSourceLine(line.substring(0, 0), "", null);

        assertSourceLine(line.substring(1, 4), "bcd", SourceSpan.of(3, 11, 14, 3));
        assertSourceLine(line.substring(1, 3), "bc", SourceSpan.of(3, 11, 14, 2));

        assertSourceLine(line.substring(3, 4), "d", SourceSpan.of(3, 13, 16, 1));
        assertSourceLine(line.substring(4, 4), "", null);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testSubstringBeginOutOfBounds() {
        SourceLine.of("abcd", SourceSpan.of(3, 10, 13, 4)).substring(3, 2);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testSubstringEndOutOfBounds() {
        SourceLine.of("abcd", SourceSpan.of(3, 10, 13, 4)).substring(0, 5);
    }

    private static void assertSourceLine(SourceLine sourceLine, String expectedContent, SourceSpan expectedSourceSpan) {
        assertEquals(expectedContent, sourceLine.getContent());
        assertEquals(expectedSourceSpan, sourceLine.getSourceSpan());
    }
}
