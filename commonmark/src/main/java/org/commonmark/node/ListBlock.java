package org.commonmark.node;

public abstract class ListBlock extends Block {

    private boolean tight;
    
    // Whitespace for roundtrip parsing of the first line in a list block
    private String whitespacePreBlock = "";
    private String whitespacePreContent = "";

    /**
     * @return whether this list is tight or loose
     * @see <a href="https://spec.commonmark.org/0.28/#tight">CommonMark Spec for tight lists</a>
     */
    public boolean isTight() {
        return tight;
    }

    public void setTight(boolean tight) {
        this.tight = tight;
    }

    public String whitespacePreBlock() {
        return whitespacePreBlock;
    }
    
    public String whitespacePreContent() {
        return whitespacePreContent;
    }
    
    public void setPreBlockWhitespace(String whitespace) {
        whitespacePreBlock = whitespace;
    }
    
    public void setPreContentWhitespace(String whitespace) {
        whitespacePreContent = whitespace;
    }
}
