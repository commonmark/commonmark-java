package org.commonmark.node;

public class BlockQuote extends Block {
    // Whitespace for roundtrip rendering
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
}
