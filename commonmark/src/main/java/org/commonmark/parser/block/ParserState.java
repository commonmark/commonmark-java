package org.commonmark.parser.block;

/**
 * State of the parser that is used in block parsers.
 * <p><em>This interface is not intended to be implemented by clients.</em></p>
 */
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
     * @return the current column within the line
     */
    int getColumn();

    /**
     * @return the indentation (either by spaces or tab stop of 4), starting from {@link #getColumn()}
     */
    int getIndent();

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
