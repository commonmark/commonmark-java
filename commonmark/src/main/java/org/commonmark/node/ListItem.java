package org.commonmark.node;

public class ListItem extends Block {
    private String rawNumber = "";
    
    private String whitespacePreMarker = "";
    private String whitespacePostMarker = "";

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
    
    public String whitespacePreMarker() {
        return whitespacePreMarker;
    }

    public String whitespacePostMarker() {
        return whitespacePostMarker;
    }
    
    public void setPreMarkerWhitespace(String whitespace) {
        whitespacePreMarker = whitespace;
    }
    
    public void setPostMarkerWhitespace(String whitespace) {
        whitespacePostMarker = whitespace;
    }
    
    public String getRawNumber() {
        return rawNumber;
    }
    
    public void setRawNumber(String rawNumber) {
        this.rawNumber = rawNumber;
    }
}
