package org.commonmark.internal;

import org.commonmark.node.Text;
import org.commonmark.parser.delimiter.DelimiterRun;

/**
 * Delimiter (emphasis, strong emphasis or custom emphasis).
 */
public class Delimiter implements DelimiterRun {

    public final Text node;
    public final char delimiterChar;

    /**
     * Can open emphasis, see spec.
     */
    public final boolean canOpen;

    /**
     * Can close emphasis, see spec.
     */
    public final boolean canClose;

    public Delimiter previous;
    public Delimiter next;

    public int length = 1;
    public int originalLength = 1;

    public Delimiter(Text node, char delimiterChar, boolean canOpen, boolean canClose, Delimiter previous) {
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
        return length;
    }

    @Override
    public int originalLength() {
        return originalLength;
    }
}
