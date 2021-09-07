package org.commonmark.node;

public class OrderedList extends ListBlock {
    private int startNumber;
    private String rawNumber;
    private char delimiter;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public int getStartNumber() {
        return startNumber;
    }

    public void setStartNumber(int startNumber) {
        this.startNumber = startNumber;
    }
    
    public String getRawNumber() {
        return rawNumber;
    }

    public void setRawNumber(String rawNumber) {
        this.rawNumber = rawNumber;
    }

    public char getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(char delimiter) {
        this.delimiter = delimiter;
    }
}
