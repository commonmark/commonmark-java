package org.commonmark.node;

public class Heading extends Block {
    private int level;
    private char symbolType;
    private int numEndingSymbol;
    
    // Whitespace for roundtrip rendering
    // AST: Pre-marker whitespace (ATX) or before setext text (setext)
    private String whitespacePreBlock = "";
    // AST: Pre-text (ATX) or after setext text ends (setext)
    private String whitespacePreContent = "";
    // AST: Post-text (ATX) or before setext delimiting line (setext)
    private String whitespacePostContent = "";
    // AST: Post-block (ATX) or after setext delimiting line (setext) 
    private String whitespacePostBlock = "";

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
    
    public String whitespacePreBlock() {
        return whitespacePreBlock;
    }
    
    public String whitespacePreContent() {
        return whitespacePreContent;
    }
    
    public String whitespacePostContent() {
        return whitespacePostContent;
    }
    
    public String whitespacePostBlock() {
        return whitespacePostBlock;
    }
    
    public void setPreBlockWhitespace(String whitespace) {
        whitespacePreBlock = whitespace;
    }
    
    public void setPreContentWhitespace(String whitespace) {
        whitespacePreContent = whitespace;
    }
    
    public void setPostContentWhitespace(String whitespace) {
        whitespacePostContent = whitespace;
    }
    
    public void setPostBlockWhitespace(String whitespace) {
        whitespacePostBlock = whitespace;
    }
}
