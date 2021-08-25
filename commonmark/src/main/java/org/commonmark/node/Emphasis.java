package org.commonmark.node;

public class Emphasis extends Node implements Delimited {

    private String delimiter;
    private String preBlockWhitespace = "";

    public Emphasis() {
    }

    public Emphasis(String delimiter) {
        this.delimiter = delimiter;
    }
    
    public Emphasis(String delimiter, String preBlockWhitespace) {
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
