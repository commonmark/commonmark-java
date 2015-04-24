package org.commonmark.internal.util;

/**
 * A CharSequence that avoids copying string data when getting a substring.
 */
public class Substring implements CharSequence {

    private final String base;
    private final int beginIndex;
    private final int endIndex;

    public static CharSequence of(String base, int beginIndex, int endIndex) {
        return new Substring(base, beginIndex, endIndex);
    }

    private Substring(String base, int beginIndex, int endIndex) {
        this.base = base;
        this.beginIndex = beginIndex;
        this.endIndex = endIndex;
        if (endIndex > base.length()) {
            throw new IndexOutOfBoundsException("endIndex must not be greater than length");
        }
    }

    @Override
    public int length() {
        return endIndex - beginIndex;
    }

    @Override
    public char charAt(int index) {
        return base.charAt(index + beginIndex);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return new Substring(base, beginIndex + start, beginIndex + end);
    }

    @Override
    public String toString() {
        return base.substring(beginIndex, endIndex);
    }
}
