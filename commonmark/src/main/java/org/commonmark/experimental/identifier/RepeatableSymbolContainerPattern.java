package org.commonmark.experimental.identifier;

import org.commonmark.experimental.NodeBreakLinePattern;

public class RepeatableSymbolContainerPattern implements NodeBreakLinePattern {
    private final char symbol;
    private final int size;
    private final int minSize;

    public RepeatableSymbolContainerPattern(char symbol, int size) {
        this.symbol = symbol;
        this.size = size;
        this.minSize = 1;
    }

    @Override
    public char characterTrigger() {
        return symbol;
    }

    public int getSize() {
        return size;
    }

    public int getMinSize() {
        return minSize;
    }
}
