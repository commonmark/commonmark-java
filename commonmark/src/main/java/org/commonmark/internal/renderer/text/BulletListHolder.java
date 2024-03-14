package org.commonmark.internal.renderer.text;

import org.commonmark.node.BulletList;

public class BulletListHolder extends ListHolder {
    private final String marker;

    public BulletListHolder(ListHolder parent, BulletList list) {
        super(parent);
        marker = list.getMarker();
    }

    public String getMarker() {
        return marker;
    }
}
