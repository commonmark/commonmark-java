package org.commonmark.internal;

import org.commonmark.node.Block;
import org.commonmark.node.Node;

public interface BlockParser {

    // TODO: Change params to interface, similar to ParserState
    ContinueResult continueBlock(CharSequence line, int nextNonSpace, int offset, boolean blank);

    boolean canContain(Block block);

    boolean shouldTryBlockStarts();

    /**
     * Returns true if block type can accept lines of text
     */
    boolean acceptsLine();

    void addLine(CharSequence line);

    void finalizeBlock(InlineParser inlineParser);

    void processInlines(InlineParser inlineParser);

    Block getBlock();

    interface ContinueResult {
    }

    interface BlockMatched extends ContinueResult {
        int getNewOffset();
    }

    interface BlockDidNotMatch extends ContinueResult {
    }

    interface BlockMatchedAndCanBeFinalized extends ContinueResult {
    }
}
