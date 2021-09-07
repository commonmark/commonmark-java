package org.commonmark.node;

public class IndentedCodeBlock extends Block {
    // The "literal" string is optimized for HTML formatting, but omits some details
    //    which are important for roundtrip rendering
    private String literal;
    
    // The "raw" string is, as much as possible, the entire raw content as it was first
    //    entered into the parser. This allows roundtrip parsing for indented code blocks.
    private String raw;
    
    // Actual indent whitespace of a raw string for roundtrip rendering
    private String indentWhitespace = "";

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
    
    public String getRaw() {
        return raw;
    }
    
    public void setRaw(String raw) {
        this.raw = raw;
    }
    
    public String getIndentWhitespace() {
        return indentWhitespace;
    }
    
    public void setIndentWhitespace(String whitespace) {
        indentWhitespace = whitespace;
    }
}
