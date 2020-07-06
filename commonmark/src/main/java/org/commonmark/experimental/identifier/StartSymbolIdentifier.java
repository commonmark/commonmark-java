package org.commonmark.experimental.identifier;

import org.commonmark.experimental.NodePatternIdentifier;
import org.commonmark.experimental.TextIdentifier;

public class StartSymbolIdentifier extends TextIdentifier {
    private final StartSymbolPattern nodeBreakLinePattern;

    public StartSymbolIdentifier(StartSymbolPattern nodeBreakLinePattern) {
        super(nodeBreakLinePattern);
        this.nodeBreakLinePattern = nodeBreakLinePattern;
    }

    @Override
    public void checkByCharacter(String text, char character, int index, NodePatternIdentifier nodePatternIdentifier) {

        if (startIndex == INVALID_INDEX
                && character == nodeBreakLinePattern.characterTrigger()
                && isFoundStart(text, index)
        ) {
            startIndex = index;
        } else if (startIndex != INVALID_INDEX
                && endIndex == INVALID_INDEX
                && isFoundEnd(text, index)) {
            endIndex = index + 1;
        }

        if (startIndex != INVALID_INDEX && endIndex != INVALID_INDEX) {
            nodePatternIdentifier.found(startIndex, endIndex, null);
            reset();
        }
    }

    private boolean isFoundStart(String text, int index) {
        return index == 0
                || isNotDigitAndIsNotLetter(text.charAt(index - 1));
    }

    private boolean isFoundEnd(String text, int index) {
        int lastIndex = text.length() - 1;
        return index == lastIndex
                || (index < lastIndex
                && isNotInDontStopList(text.charAt(index + 1))
                && isNotDigitAndIsNotLetter(text.charAt(index + 1))
        );
    }

    private boolean isNotInDontStopList(char charAt) {
        return !nodeBreakLinePattern.getDontStopAt().get(charAt);
    }

    private boolean isNotDigitAndIsNotLetter(char character) {
        return !Character.isDigit(character)
                && !Character.isLetter(character);
    }
}
