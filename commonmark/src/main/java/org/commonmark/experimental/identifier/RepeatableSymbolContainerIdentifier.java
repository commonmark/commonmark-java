package org.commonmark.experimental.identifier;

import org.commonmark.experimental.NodePatternIdentifier;
import org.commonmark.experimental.TextIdentifier;

public class RepeatableSymbolContainerIdentifier extends TextIdentifier {
    private final RepeatableSymbolContainerPattern nodeBreakLinePattern;
    private final int patternSizeLessOne;
    private int countStartSymbols;
    private int countEndSymbols;

    public RepeatableSymbolContainerIdentifier(RepeatableSymbolContainerPattern nodeBreakLinePattern) {
        super(nodeBreakLinePattern);
        this.nodeBreakLinePattern = nodeBreakLinePattern;
        this.patternSizeLessOne = nodeBreakLinePattern.getSize() - 1;
    }

    @Override
    public void checkByCharacter(String text, char character, int index, NodePatternIdentifier nodePatternIdentifier) {

        final boolean symbolMatch = character == nodeBreakLinePattern.characterTrigger();

        resetCounterIfSymbolDoesNotMatch(symbolMatch);

        if (startIndex == INVALID_INDEX && symbolMatch) {
            if (countStartSymbols == patternSizeLessOne) {
                startIndex = index - countStartSymbols;
            } else {
                countStartSymbols++;
            }
        } else if (endIndex == INVALID_INDEX && symbolMatch
                && index - startIndex - countStartSymbols - 1 > nodeBreakLinePattern.getMinSize()) {
            if (countEndSymbols == patternSizeLessOne) {
                endIndex = index + 1;
            } else {
                countEndSymbols++;
            }
        }

        if (startIndex != INVALID_INDEX && endIndex != INVALID_INDEX) {
            nodePatternIdentifier.found(startIndex, endIndex, null);
            reset();
        }
    }

    private void resetCounterIfSymbolDoesNotMatch(boolean symbolMatch) {
        if (countStartSymbols > 0 && !symbolMatch) {
            countStartSymbols = 0;
        }
        if (countEndSymbols > 0 && !symbolMatch) {
            countEndSymbols = 0;
        }
    }

    @Override
    protected void reset() {
        super.reset();
        countStartSymbols = 0;
        countEndSymbols = 0;
    }
}
