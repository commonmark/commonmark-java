package org.commonmark.experimental.identifier;

import org.commonmark.experimental.NodePatternIdentifier;
import org.commonmark.experimental.TextIdentifier;

import java.util.List;

public class BracketContainerIdentifier extends TextIdentifier {
    private final List<BracketContainerPattern.OpenClose> openCloses;
    private final BracketContainerPattern nodeBreakLinePattern;
    private NodePatternIdentifier.InternalBlocks[] internalBlocks;
    private int countBrackets = 0;
    private int indexOpenClose = 0;
    private boolean nextCharacterOpenExpected = false;
    private boolean lastGroupClosed = false;
    private BracketContainerPattern.OpenClose currentOpenClose;

    public BracketContainerIdentifier(BracketContainerPattern nodeBreakLinePattern) {
        super(nodeBreakLinePattern);
        this.openCloses = nodeBreakLinePattern.getOpenCloses();
        this.nodeBreakLinePattern = nodeBreakLinePattern;
        this.currentOpenClose = openCloses.get(indexOpenClose);
        this.internalBlocks = new NodePatternIdentifier.InternalBlocks[openCloses.size()];
    }

    @Override
    public void checkByCharacter(String text, char character, int index, NodePatternIdentifier nodePatternIdentifier) {
        boolean isOpenCharacter = currentOpenClose.getOpen() == character;

        checkNextCharacterIsOpenSymbol(index, isOpenCharacter);

        if (nodeBreakLinePattern.characterTrigger() == character
                && (startIndex == INVALID_INDEX || countBrackets == 0)) {
            startIndex = index;
            nextCharacterOpenExpected = nodeBreakLinePattern.isStartBySingleSymbol();
        }

        if (startIndex == INVALID_INDEX || !isCurrentGroupContinuation(isOpenCharacter)) {
            return;
        }

        boolean countedAny = false;
        if (isOpenCharacter) {
            countBrackets++;
            countedAny = true;
        } else if (currentOpenClose.getClose() == character) {
            countBrackets--;
            countedAny = true;
        }

        if (countedAny && countBrackets == 0) {
            int startIndexGroup = indexOpenClose == 0
                    ? 0
                    : internalBlocks[indexOpenClose - 1].getRelativeEndIndex();

            internalBlocks[indexOpenClose] = new NodePatternIdentifier.InternalBlocks(
                    startIndexGroup,
                    index - startIndex + 1);

            if (indexOpenClose < openCloses.size() - 1) {
                indexOpenClose++;
                currentOpenClose = openCloses.get(indexOpenClose);
                lastGroupClosed = true;
            } else {
                endIndex = index + 1;
            }
        }

        if (startIndex != INVALID_INDEX && endIndex != INVALID_INDEX) {
            nodePatternIdentifier.found(startIndex, endIndex, internalBlocks);
            reset();
        }
    }

    private void checkNextCharacterIsOpenSymbol(int index, boolean isOpenCharacter) {
        if (nextCharacterOpenExpected && startIndex == index - 1 && !isOpenCharacter) {
            startIndex = INVALID_INDEX;
            nextCharacterOpenExpected = false;
        }
    }

    private boolean isCurrentGroupContinuation(final boolean isOpenCharacter) {
        if (lastGroupClosed && !isOpenCharacter) {
            reset();
            return false;
        } else {
            lastGroupClosed = false;
            return true;
        }
    }

    @Override
    protected void reset() {
        super.reset();
        this.countBrackets = 0;
        this.indexOpenClose = 0;
        this.currentOpenClose = openCloses.get(indexOpenClose);
        this.lastGroupClosed = false;
        this.nextCharacterOpenExpected = false;
        this.internalBlocks = new NodePatternIdentifier.InternalBlocks[openCloses.size()];
    }
}
