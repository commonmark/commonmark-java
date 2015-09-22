package org.commonmark.node;

public class StrongEmphasis extends Node implements Delimited {

    private char delimiterChar;
    private int delimiterCount;

    public StrongEmphasis(char delimiterChar, int delimiterCount) {
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
