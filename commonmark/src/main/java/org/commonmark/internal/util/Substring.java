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
        if (beginIndex < 0) {
            throw new StringIndexOutOfBoundsException("beginIndex must be at least 0");
        }
        if (endIndex < 0) {
            throw new StringIndexOutOfBoundsException("endIndex must be at least 0");
        }
        if (endIndex < beginIndex) {
            throw new StringIndexOutOfBoundsException("endIndex must not be less than beginIndex");
        }
        if (endIndex > base.length()) {
            throw new StringIndexOutOfBoundsException("endIndex must not be greater than length");
        }
        this.base = base;
        this.beginIndex = beginIndex;
        this.endIndex = endIndex;
    }

    @Override
    public int length() {
        return endIndex - beginIndex;
    }

    @Override
    public char charAt(int index) {
        if (index < 0 || beginIndex + index >= endIndex) {
            throw new StringIndexOutOfBoundsException("String index out of range: " + index);
        }
        return base.charAt(index + beginIndex);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        if (start < 0 || beginIndex + start > endIndex) {
            throw new StringIndexOutOfBoundsException("String index out of range: " + start);
        }
        if (end < 0 || beginIndex + end > endIndex) {
            throw new StringIndexOutOfBoundsException("String index out of range: " + end);
        }
        return new Substring(base, beginIndex + start, beginIndex + end);
    }

    @Override
    public String toString() {
        return base.substring(beginIndex, endIndex);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof CharSequence && toString().equals(obj.toString()));
    }
}
