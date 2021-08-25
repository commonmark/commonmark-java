package org.commonmark.node;

public class Heading extends Block {
	// Track whitespace as follows:
    //    [0] Pre-block
    //    [1] Pre-content
    //    [2] Post-content
    //    [3] Post-block
    private String[] whitespaceTracker = {"", "", "", ""};

    private int level;
    private char symbolType;
    private int numEndingSymbol;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
    
    public char getSymbolType() {
        return symbolType;
    }

    public void setSymbolType(char symbolType) {
        this.symbolType = symbolType;
    }

    public int getNumEndingSymbol() {
        return numEndingSymbol;
    }

    public void setNumEndingSymbol(int numEndingSymbol) {
        this.numEndingSymbol = numEndingSymbol;
    }
    
    @Override
    public String whitespacePreBlock() {
        return whitespaceTracker[0];
    }

    @Override
    public String whitespacePreContent() {
        return whitespaceTracker[1];
    }

    @Override
    public String whitespacePostContent() {
        return whitespaceTracker[2];
    }

    @Override
    public String whitespacePostBlock() {
        return whitespaceTracker[3];
    }
    
    public void setWhitespace(String... newWhitespace) {
        whitespaceTracker = super.prepareStructuralWhitespace(newWhitespace);
    }
}
