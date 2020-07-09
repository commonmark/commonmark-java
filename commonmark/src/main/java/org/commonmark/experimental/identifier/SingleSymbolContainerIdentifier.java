package org.commonmark.experimental.identifier;

import org.commonmark.experimental.NodePatternIdentifier;
import org.commonmark.experimental.TextIdentifier;

public class SingleSymbolContainerIdentifier extends TextIdentifier {
    private final SingleSymbolContainerPattern nodeBreakLinePattern;
    private char lastCharacter = INVALID_CHAR;

    public SingleSymbolContainerIdentifier(SingleSymbolContainerPattern nodeBreakLinePattern) {
        super(nodeBreakLinePattern);
        this.nodeBreakLinePattern = nodeBreakLinePattern;
    }

    @Override
    public void checkByCharacter(String text, char character, int index, NodePatternIdentifier nodePatternIdentifier) {
        if (character == nodeBreakLinePattern.characterTrigger()) {
            if (startIndex == INVALID_INDEX) {
                startIndex = index;
            } else {
                if (lastCharacter == ' ' || index - startIndex <= nodeBreakLinePattern.getMinSize()) {
                    reset();
                    startIndex = index;
                } else {
                    endIndex = index + 1;
                }

                if (endIndex != INVALID_INDEX) {
                    nodePatternIdentifier.found(startIndex, endIndex, null);
                    reset();
                }
            }
        } else if (index == startIndex + 1 && character == ' ') {
            reset();
        }

        lastCharacter = character;
    }

    @Override
    protected void reset() {
        super.reset();
        lastCharacter = '\0';
    }
}
