package org.commonmark.experimental.identifier;

import org.commonmark.experimental.NodeBreakLinePattern;

public class SingleSymbolContainerPattern implements NodeBreakLinePattern {
    private final char symbol;
    private final int minSize;

    public SingleSymbolContainerPattern(char symbol) {
        this(symbol, 1);
    }

    public SingleSymbolContainerPattern(char symbol, int minSize) {
        this.symbol = symbol;
        this.minSize = minSize;
    }

    @Override
    public char characterTrigger() {
        return symbol;
    }

    public int getMinSize() {
        return minSize;
    }
}
