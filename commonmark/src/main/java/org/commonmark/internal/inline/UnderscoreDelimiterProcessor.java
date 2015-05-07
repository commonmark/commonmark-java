package org.commonmark.internal.inline;

public class UnderscoreDelimiterProcessor extends EmphasisDelimiterProcessor {
    @Override
    public char getDelimiterChar() {
        return '_';
    }
}
