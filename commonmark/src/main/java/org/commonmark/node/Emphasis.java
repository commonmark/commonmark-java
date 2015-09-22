package org.commonmark.node;

public class Emphasis extends Node implements Delimited {

    private final char delimiterChar;
    private final int delimiterCount;

    public Emphasis(char delimiterChar, int delimiterCount) {
        this.delimiterChar = delimiterChar;
        this.delimiterCount = delimiterCount;
    }

    @Override
    public char getDelimiterChar() {
        return delimiterChar;
    }

    @Override
    public int getDelimiterCount() {
        return delimiterCount;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
