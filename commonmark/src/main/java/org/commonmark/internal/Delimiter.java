package org.commonmark.internal;

import org.commonmark.node.Text;
import org.commonmark.parser.delimiter.DelimiterRun;

/**
 * Delimiter (emphasis, strong emphasis or custom emphasis).
 */
class Delimiter implements DelimiterRun {

    final Text node;
    final char delimiterChar;

    /**
     * Can open emphasis, see spec.
     */
    final boolean canOpen;

    /**
     * Can close emphasis, see spec.
     */
    final boolean canClose;

    Delimiter previous;
    Delimiter next;

    int numDelims = 1;

    Delimiter(Text node, char delimiterChar, boolean canOpen, boolean canClose, Delimiter previous) {
        this.node = node;
        this.delimiterChar = delimiterChar;
        this.canOpen = canOpen;
        this.canClose = canClose;
        this.previous = previous;
    }

    @Override
    public boolean canOpen() {
        return canOpen;
    }

    @Override
    public boolean canClose() {
        return canClose;
    }

    @Override
    public int length() {
        return numDelims;
    }
}
