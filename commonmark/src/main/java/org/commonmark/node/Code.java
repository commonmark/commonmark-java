package org.commonmark.node;

public class Code extends Node {

    private String literal;
    private String raw;
    private boolean strippedSpaces;
    private int numBackticks;

    public Code() {
    }

    public Code(String literal) {
        this.literal = literal;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public String getLiteral() {
        return literal;
    }

    public void setLiteral(String literal) {
        this.literal = literal;
    }
    
    public boolean hasStrippedSpaces() {
        return strippedSpaces;
    }

    public void setStrippedSpaces(boolean strippedSpaces) {
        this.strippedSpaces = strippedSpaces;
    }

    public int getNumBackticks() {
        return numBackticks;
    }

    public void setNumBackticks(int numBackticks) {
        this.numBackticks = numBackticks;
    }
    
    public String getRaw() {
        return raw;
    }

    public void setRaw(String raw) {
        this.raw = raw;
    }
}
