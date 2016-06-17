package org.commonmark.internal;

import org.commonmark.node.Text;

/**
 * Delimiter (emphasis, strong emphasis or custom emphasis).
 */
class Delimiter {

    final Text node;

    Delimiter previous;
    Delimiter next;

    char delimiterChar;
    int numDelims = 1;

    /**
     * Can open emphasis, see spec.
     */
    boolean canOpen = true;

    /**
     * Can close emphasis, see spec.
     */
    boolean canClose = false;

    Delimiter(Text node, Delimiter previous) {
        this.node = node;
        this.previous = previous;
    }
}
