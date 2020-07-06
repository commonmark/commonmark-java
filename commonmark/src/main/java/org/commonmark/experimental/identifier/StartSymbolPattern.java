package org.commonmark.experimental.identifier;

import org.commonmark.experimental.NodeBreakLinePattern;

import java.util.BitSet;

public class StartSymbolPattern implements NodeBreakLinePattern {
    private final char startSymbol;
    private final BitSet dontStopAt;

    public StartSymbolPattern(char startSymbol) {
        this(startSymbol, new char[0]);
    }

    public StartSymbolPattern(char startSymbol, char... dontStopAt) {
        this.startSymbol = startSymbol;
        this.dontStopAt = new BitSet();
        fillDontStopCharacterSet(dontStopAt);
    }

    public BitSet getDontStopAt() {
        return dontStopAt;
    }

    private void fillDontStopCharacterSet(char[] dontStopAt) {
        for (Character character : dontStopAt) {
            this.dontStopAt.set(character);
        }
    }

    @Override
    public char characterTrigger() {
        return startSymbol;
    }
}
