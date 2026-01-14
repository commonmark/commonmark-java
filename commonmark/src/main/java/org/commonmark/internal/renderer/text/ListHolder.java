package org.commonmark.internal.renderer.text;

public abstract class ListHolder {
    private final ListHolder parent;

    ListHolder(ListHolder parent) {
        this.parent = parent;
    }

    public ListHolder getParent() {
        return parent;
    }
}
