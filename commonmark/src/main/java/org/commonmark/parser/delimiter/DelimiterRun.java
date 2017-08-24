package org.commonmark.parser.delimiter;

/**
 * A delimiter run is one or more of the same delimiter character.
 */
public interface DelimiterRun {

    /**
     * @return whether this can open a delimiter
     */
    boolean canOpen();

    /**
     * @return whether this can close a delimiter
     */
    boolean canClose();

    /**
     * @return the number of characters in this delimiter run (that are left for processing)
     */
    int length();

    /**
     * @return the number of characters originally in this delimiter run; at the start of processing, this is the same
     * as {{@link #length()}}
     */
    int originalLength();
}
