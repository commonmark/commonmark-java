package org.commonmark.internal;

import org.commonmark.node.Block;
import org.commonmark.node.Node;
import org.commonmark.parser.BlockContinue;

public interface BlockParser {

    BlockContinue tryContinue(ParserState parserState);

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

}
