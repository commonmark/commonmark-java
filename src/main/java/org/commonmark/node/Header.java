package org.commonmark.node;

public class Header extends Block {

    private int level;

    @Override
    public Type getType() {
        return Type.Header;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
