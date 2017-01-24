package org.commonmark.internal;

/**
 * Parser for inline references
 */
public interface ReferenceParser {
    /**
     * @return how many characters were parsed as a reference, {@code 0} if none
     */
    int parseReference(String s);
}
