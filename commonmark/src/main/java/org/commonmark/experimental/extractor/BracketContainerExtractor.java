package org.commonmark.experimental.extractor;

import org.commonmark.experimental.NodePatternIdentifier.InternalBlocks;
import org.commonmark.experimental.identifier.BracketContainerPattern;

public class BracketContainerExtractor {
    public static final String[] EMPTY_STRING = new String[0];

    private BracketContainerExtractor() {
    }

    public static String[] from(String text,
                                BracketContainerPattern bracketContainerPattern,
                                InternalBlocks[] internalBlocks) {
        if (text == null || internalBlocks.length == 0) {
            return EMPTY_STRING;
        }

        int factorBegin = bracketContainerPattern.isStartBySingleSymbol() ? 1 : 0;
        String[] contents = new String[internalBlocks.length];
        for (int i = 0; i < internalBlocks.length; i++) {
            int beginIndex = internalBlocks[i].getRelativeStartIndex() + factorBegin + 1;

            contents[i] = text.substring(
                    beginIndex,
                    internalBlocks[i].getRelativeEndIndex() - 1);
            factorBegin = 0;
        }
        return contents;
    }
}
