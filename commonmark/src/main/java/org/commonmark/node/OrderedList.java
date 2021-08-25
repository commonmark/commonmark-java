package org.commonmark.node;

public class OrderedList extends ListBlock {
	// Track whitespace as follows:
    //    [0] Pre-block
    //    [1] Pre-content
    //    [2] Post-content
    //    [3] Post-block
    private String[] whitespaceTracker = {"", "", "", ""};

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
