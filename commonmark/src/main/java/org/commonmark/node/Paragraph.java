package org.commonmark.node;

/**
 * A paragraph block, contains inline nodes such as {@link Text}
 */
public class Paragraph extends Block {
    // Whitespace for roundtrip rendering
    private String whitespacePreBlock = "";

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public String whitespacePreBlock() {
        return whitespacePreBlock;
    }
    
    public void setPreBlockWhitespace(String whitespace) {
        whitespacePreBlock = whitespace;
    }
}
