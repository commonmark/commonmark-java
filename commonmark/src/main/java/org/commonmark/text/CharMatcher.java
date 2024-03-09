package org.commonmark.text;

/**
 * Matcher interface for {@code char} values.
 * <p>
 * Note that because this matches on {@code char} values only (as opposed to {@code int} code points),
 * this only operates on the level of code units and doesn't support supplementary characters
 * (see {@link Character#isSupplementaryCodePoint(int)}).
 */
public interface CharMatcher {

    boolean matches(char c);
}
