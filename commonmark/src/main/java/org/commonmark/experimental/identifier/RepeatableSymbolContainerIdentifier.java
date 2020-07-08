package org.commonmark.experimental.identifier;

import org.commonmark.experimental.NodePatternIdentifier;
import org.commonmark.experimental.TextIdentifier;

public class RepeatableSymbolContainerIdentifier extends TextIdentifier {
    private final RepeatableSymbolContainerPattern nodeBreakLinePattern;
    private final int patternSizeLessOne;
    private int countStartSymbols;
    private int countEndSymbols;
    private char lastCharacter = INVALID_CHAR;

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
        } else if (isCharacterAfterStartSymbolsSpace(character, index)) {
            reset();
        } else if (endIndex == INVALID_INDEX
                && symbolMatch
                && isContentGreaterThanMinSize(index)) {
            if (countEndSymbols == patternSizeLessOne) {
                if (isLastCharacterBeforeCloseSymbolSpace(text, index)) {
                    reset();
                } else {
                    endIndex = index + 1;
                }
            } else {
                countEndSymbols++;
            }
        }

        if (startIndex != INVALID_INDEX && endIndex != INVALID_INDEX) {
            nodePatternIdentifier.found(startIndex, endIndex, null);
            reset();
        }
    }

    private boolean isLastCharacterBeforeCloseSymbolSpace(String text, int index) {
        return text.charAt(index - patternSizeLessOne - 1) == ' ';
    }

    private boolean isContentGreaterThanMinSize(int index) {
        return index - startIndex - countStartSymbols - 1 > nodeBreakLinePattern.getMinSize();
    }

    private boolean isCharacterAfterStartSymbolsSpace(char character, int index) {
        return index == startIndex + patternSizeLessOne + 1 && character == ' ';
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
        lastCharacter = INVALID_CHAR;
    }
}
