package org.commonmark.node;

/**
 * Block nodes such as paragraphs, list blocks, code blocks etc.
 */
public abstract class Block extends Node {

    public Block getParent() {
        return (Block) super.getParent();
    }

    @Override
    protected void setParent(Node parent) {
        if (!(parent instanceof Block)) {
            throw new IllegalArgumentException("Parent of block must also be block (can not be inline)");
        }
        super.setParent(parent);
    }
    
 // Roundtrip rendering requires capturing various pieces before and after blocks and their contents
    public abstract String whitespacePreBlock();
    public abstract String whitespacePreContent();
    public abstract String whitespacePostContent();
    public abstract String whitespacePostBlock();
    public abstract void setWhitespace(String...newWhitespace);
    
    /**
     * All block elements can be impacted structurally by whitespace in some way or another.
     * For convenience, this method allows passing any number of strings. However, these strings
     * are interpreted as linearly replacing one of 4 pre-defined positions (i.e
     * 2 values replace [0] and [1], 3 replace [0], [1], and [2] etc.). These 4
     * values correspond to:
     * 0. Pre-block element whitespace
     * 1. Pre-content whitespace
     * 2. Post-content whitespace
     * 3. Post-block element whitespace
     * 
     * If a caller passes more than 4 values, all values past the fourth will be ignored.
     * @param newWhitespace
     * @return Prepared array of structural whitespace
     */
    protected String[] prepareStructuralWhitespace(String... newWhitespace) {
        String[] container = {"", "", "", ""};
        
        if(newWhitespace.length == 4) {
            container = newWhitespace;
        }else {
            for(int i = 0; i < newWhitespace.length; i++) {
                container[i] = newWhitespace[i];
            }
        }
        
        return container;
    }
}
