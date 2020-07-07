package org.commonmark.experimental.identifier;

import org.commonmark.experimental.NodeBreakLinePattern;

import java.util.List;

import static java.util.Arrays.asList;

public class BracketContainerPattern implements NodeBreakLinePattern {
    public static final char INVALID_SYMBOL = '\0';
    private final char startSymbol;
    private final boolean startBySingleSymbol;
    private final List<OpenClose> openCloses;

    private BracketContainerPattern(char startSymbol, OpenClose... openClose) {
        this.openCloses = asList(openClose);
        this.startBySingleSymbol = startSymbol != INVALID_SYMBOL;
        this.startSymbol = getStartSymbol(startSymbol);
    }

    private char getStartSymbol(char startSymbol) {
        if (startSymbol != INVALID_SYMBOL) {
            return startSymbol;
        }

        if (!openCloses.isEmpty()) {
            return openCloses.get(0).open;
        }
        return INVALID_SYMBOL;
    }

    @Override
    public char characterTrigger() {
        return startSymbol;
    }

    public List<OpenClose> getOpenCloses() {
        return openCloses;
    }

    public boolean isStartBySingleSymbol() {
        return startBySingleSymbol;
    }

    public static class OpenClose {
        private final char open;
        private final char close;

        private OpenClose(char open, char close) {
            this.open = open;
            this.close = close;
        }

        public char getOpen() {
            return open;
        }

        public char getClose() {
            return close;
        }

        public static OpenClose of(char open, char close) {
            return new OpenClose(open, close);
        }
    }

    public static BracketContainerPattern of(OpenClose... openClose) {
        return BracketContainerPattern.of(INVALID_SYMBOL, openClose);
    }

    public static BracketContainerPattern of(char startSymbol, OpenClose... openClose) {
        if (openClose.length == 0) {
            throw new IllegalArgumentException("openClose must contain at least 1 configuration");
        }
        return new BracketContainerPattern(startSymbol, openClose);
    }
}
