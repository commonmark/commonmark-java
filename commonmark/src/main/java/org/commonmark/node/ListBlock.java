package org.commonmark.node;

public abstract class ListBlock extends Block {

    private boolean tight;

    public boolean isTight() {
        return tight;
    }

    public void setTight(boolean tight) {
        this.tight = tight;
    }

}
