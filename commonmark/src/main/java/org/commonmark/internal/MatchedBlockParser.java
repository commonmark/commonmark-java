package org.commonmark.internal;

public interface MatchedBlockParser {

    BlockParser getMatchedBlockParser();

    /**
     * @return the first line of the paragraph if the matched block is a paragraph and we're on the second line,
     * null otherwise
     */
    CharSequence getParagraphStartLine();

}
