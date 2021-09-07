package org.commonmark.node;

public class ThematicBreak extends Block {
    private CharSequence content;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
    
    public CharSequence getContent() {
        return content;
    }
    
    public void setContent(CharSequence content) {
        this.content = content;
    }
}
