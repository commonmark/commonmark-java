package org.commonmark.internal;

import org.commonmark.node.Text;

class Delimiter {

    final Text node;
    final int index;

    Delimiter previous;
    Delimiter next;

    char delimiterChar;
    int numDelims = 1;
    boolean canOpen = true;
    boolean canClose = false;
    boolean active = true;

    Delimiter(Text node, Delimiter previous, int index) {
        this.node = node;
        this.previous = previous;
        this.index = index;
    }
}
