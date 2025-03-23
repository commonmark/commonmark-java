package org.commonmark.node;

/**
 * A block that extensions can subclass to define custom blocks (not part of the core specification).
 */
public abstract class CustomBlock extends Block {

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
