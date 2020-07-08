package org.commonmark.experimental.identifier;

import org.commonmark.experimental.NodeBreakLinePattern;

public class SingleSymbolContainerPattern implements NodeBreakLinePattern {
    private final char symbol;
    private final int minSize;

    private SingleSymbolContainerPattern(char symbol) {
        this(symbol, 1);
    }

    private SingleSymbolContainerPattern(char symbol, int minSize) {
        this.symbol = symbol;
        this.minSize = minSize;
    }

    public static SingleSymbolContainerPattern of(char symbol) {
        return new SingleSymbolContainerPattern(symbol);
    }

    public static SingleSymbolContainerPattern of(char symbol, int minSize) {
        return new SingleSymbolContainerPattern(symbol, minSize);
    }

    @Override
    public char characterTrigger() {
        return symbol;
    }

    public int getMinSize() {
        return minSize;
    }
}
