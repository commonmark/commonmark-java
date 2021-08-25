package org.commonmark.node;

public class StrongEmphasis extends Node implements Delimited {

    private String delimiter;

    // Specifically used during roundtrip rendering of thematic breaks
    private String preBlockWhitespace;
    
    public StrongEmphasis() {
    }

    public StrongEmphasis(String delimiter) {
        this.delimiter = delimiter;
    }
    
    public StrongEmphasis(String delimiter, String preBlockWhitespace) {
        this(delimiter);
        this.preBlockWhitespace = preBlockWhitespace;
    }
    
    public String whitespacePreBlock() {
        return preBlockWhitespace;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    @Override
    public String getOpeningDelimiter() {
        return delimiter;
    }

    @Override
    public String getClosingDelimiter() {
        return delimiter;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
