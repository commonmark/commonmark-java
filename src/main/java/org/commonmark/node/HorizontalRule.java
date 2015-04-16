package org.commonmark.node;

public class HorizontalRule extends Block {

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
