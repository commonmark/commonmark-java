package org.commonmark.node;

public class HardLineBreak extends Node {
    boolean hasBackslash = false;
    
    // Preserve original default constructor by explicitly defining one
    public HardLineBreak() {
        super();
    }
    
    public HardLineBreak(boolean hasBackslash) {
        this.hasBackslash = hasBackslash;
    }
    
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
    
    public boolean hasBackslash() {
        return hasBackslash;
    }
}
