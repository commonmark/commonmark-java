package org.commonmark.experimental.identifier;

import org.commonmark.experimental.NodePatternIdentifier;
import org.commonmark.experimental.TextIdentifier;

public class SingleSymbolContainerIdentifier extends TextIdentifier {
    private final SingleSymbolContainerPattern nodeBreakLinePattern;

    public SingleSymbolContainerIdentifier(SingleSymbolContainerPattern nodeBreakLinePattern) {
        super(nodeBreakLinePattern);
        this.nodeBreakLinePattern = nodeBreakLinePattern;
    }

    @Override
    public void checkByCharacter(String text, char character, int index, NodePatternIdentifier nodePatternIdentifier) {

        if (startIndex == INVALID_INDEX && character == nodeBreakLinePattern.characterTrigger()) {
            startIndex = index;
        } else if (endIndex == INVALID_INDEX
                && character == nodeBreakLinePattern.characterTrigger()
                && index - startIndex > nodeBreakLinePattern.getMinSize() ) {
            endIndex = index + 1;
        }

        if (startIndex != INVALID_INDEX && endIndex != INVALID_INDEX) {
            nodePatternIdentifier.found(startIndex, endIndex, null);
            reset();
        }
    }
}
