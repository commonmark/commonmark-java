package org.commonmark.node;

public class Document extends Block {
    private String whitespaceEndOfDocument = "";

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
    
    public String whitespaceEndOfDocument() {
        return whitespaceEndOfDocument;
    }
    
    public void setEndOfDocumentWhitespace(String whitespace) {
        whitespaceEndOfDocument = whitespace;
    }
}
