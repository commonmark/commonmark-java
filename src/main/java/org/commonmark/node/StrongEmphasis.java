package org.commonmark.node;

public class StrongEmphasis extends Node {
    @Override
    public Type getType() {
        return Type.Strong;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
