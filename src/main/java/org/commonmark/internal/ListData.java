package org.commonmark.internal;

import org.commonmark.node.ListBlock;

class ListData {

    ListBlock.ListType type;
    boolean tight = true;
    char bulletChar;
    int start; // null
    char delimiter;

    int markerOffset;
    int padding = 0; // null

    public ListData(int indent) {
        this.markerOffset = indent;
    }

}
