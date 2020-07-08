package org.commonmark.experimental.identifier;

import org.commonmark.experimental.NodeBreakLinePattern;

public class RepeatableSymbolContainerPattern implements NodeBreakLinePattern {
    private final char symbol;
    private final int size;
    private final int minSize;
    private final String literalSymbolContainer;

    private RepeatableSymbolContainerPattern(char symbol, int size) {
        this.symbol = symbol;
        this.size = size;
        this.minSize = 1;
        this.literalSymbolContainer = buildLiteralText();
    }

    public static RepeatableSymbolContainerPattern of(char symbol, int size) {
        return new RepeatableSymbolContainerPattern(symbol, size);
    }

    private String buildLiteralText() {
        char[] repeatedCharacters = new char[size];
        for (int i = 0; i < size; i++) {
            repeatedCharacters[i] = symbol;
        }
        return new String(repeatedCharacters);
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

    public String literalSymbolContainer() {
        return literalSymbolContainer;
    }
}
