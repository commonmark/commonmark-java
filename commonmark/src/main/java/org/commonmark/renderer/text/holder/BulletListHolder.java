package org.commonmark.renderer.text.holder;

import org.commonmark.node.BulletList;

public class BulletListHolder extends ListHolder {
    private final char marker;

    public BulletListHolder(BulletListHolder parent, BulletList list) {
        super(parent);
        marker = list.getBulletMarker();
    }

    public char getMarker() {
        return marker;
    }
}
