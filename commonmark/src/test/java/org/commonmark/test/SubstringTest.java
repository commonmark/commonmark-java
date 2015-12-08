package org.commonmark.test;

import org.commonmark.internal.util.Substring;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class SubstringTest {

    private final CharSequence substring = Substring.of("abcdefghi", 3, 6);

    @Test
    public void testConstructEmpty() {
        assertEquals("", Substring.of("ab", 0, 0).toString());
        assertEquals("", Substring.of("ab", 1, 1).toString());
        assertEquals("", Substring.of("ab", 2, 2).toString());
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testConstructBeginIndexNegative() {
        Substring.of("abc", -1, 0);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testConstructEndIndexNegative() {
        Substring.of("abc", 0, -1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testConstructEndIndexLessThanBegin() {
        Substring.of("abc", 1, 0);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testConstructEndIndexGreaterThanLength() {
        Substring.of("abc", 1, 4);
    }

    @Test
    public void testLength() {
        assertEquals(3, substring.length());
    }

    @Test
    public void testCharAt() {
        assertEquals('d', substring.charAt(0));
        assertEquals('e', substring.charAt(1));
        assertEquals('f', substring.charAt(2));
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testCharAtOutOfBoundsLeft() {
        substring.charAt(-1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testCharAtOutOfBoundsRight() {
        substring.charAt(3);
    }

    @Test
    public void testSubSequence() {
        assertEquals("d", substring.subSequence(0, 1).toString());
        assertEquals("e", substring.subSequence(1, 2).toString());
        assertEquals("f", substring.subSequence(2, 3).toString());
        assertEquals("def", substring.subSequence(0, 3).toString());
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testSubSequenceOutOfBoundsLeft() {
        substring.subSequence(-1, 2);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testSubSequenceOutOfBoundsRight() {
        substring.subSequence(1, 4);
    }

    @Test
    public void testHashCodeEquals() {
        CharSequence a = Substring.of("abcdefghi", 3, 6);
        CharSequence b = Substring.of("123def456", 3, 6);
        CharSequence other = Substring.of("123de", 3, 5);
        assertEquals(a, b);
        assertEquals(b, a);
        assertNotEquals(a, other);
        assertNotEquals(other, a);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
