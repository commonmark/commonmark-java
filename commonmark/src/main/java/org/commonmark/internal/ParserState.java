package org.commonmark.internal;

public interface ParserState {

    /**
     * @return the current line
     */
    CharSequence getLine();

    /**
     * @return the current index within the line
     */
    int getIndex();

    /**
     * @return the index of the next non-space character starting from {@link #getIndex()} (may be the same)
     */
    int getNextNonSpaceIndex();

    /**
     * @return true if the current line is blank starting from the index
     */
    boolean isBlank();

    /**
     * @return the deepest open block parser
     */
    BlockParser getActiveBlockParser();

    int getLineNumber();

}
