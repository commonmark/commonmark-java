package org.commonmark.parser;

import org.commonmark.node.Text;

/**
 * Custom delimiter processor for additional delimiters besides {@code _} and {@code *}.
 * <p>
 * Note that implementations of this need to be thread-safe, the same instance may be used by multiple parsers.
 */
public interface DelimiterProcessor {

    /**
     * @return the character that marks the beginning of a delimited node, must not clash with any built-in special
     * characters
     */
    char getOpeningDelimiterChar();

    /**
     * @return the character that marks the the ending of a delimited node, must not clash with any built-in special
     * characters. Note that for a symmetric delimiter such as "*", this is the same as the opening.
     */
    char getClosingDelimiterChar();

    /**
     * Minimum number of delimiter characters that are needed to activate this. Must be at least 1.
     */
    int getMinDelimiterCount();

    /**
     * Determine how many of the delimiters should be used. Useful in case the same character with a different count
     * should have a different meaning (e.g. with "*" for emphasis and "**" for strong emphasis).
     *
     * @param openerCount the delimiter count of the opening delimiter, at least 1
     * @param closerCount the delimiter count of the closing delimiter, at least 1
     * @return how many delimiters should be used; cannot be 0; must not be greater than either openerCount or closerCount
     */
    int getDelimiterUse(int openerCount, int closerCount);

    /**
     * Process the matched delimiters, e.g. by wrapping the nodes between opener and closer in a new node, or appending
     * a new node after the opener.
     * <p>
     * Note that removal of the delimiter from the delimiter nodes and unlinking them is done by the caller.
     *
     * @param opener the text node that contained the opening delimiter
     * @param closer the text node that contained the closing delimiter
     * @param delimiterUse the number of delimiters that were used
     */
    void process(Text opener, Text closer, int delimiterUse);

}
