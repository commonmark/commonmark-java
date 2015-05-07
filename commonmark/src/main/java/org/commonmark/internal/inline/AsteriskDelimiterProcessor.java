package org.commonmark.internal.inline;

public class AsteriskDelimiterProcessor extends EmphasisDelimiterProcessor {
    @Override
    public char getDelimiterChar() {
        return '*';
    }
}
