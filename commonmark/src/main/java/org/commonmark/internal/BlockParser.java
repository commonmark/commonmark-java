package org.commonmark.internal;

import org.commonmark.node.Block;
import org.commonmark.parser.BlockContinue;

public interface BlockParser {

    /**
     * Return true if the block that is parsed is a container (contains other blocks), or false if it's a leaf.
     */
    boolean isContainer();

    boolean canContain(Block block);

    Block getBlock();

    BlockContinue tryContinue(ParserState parserState);

    void addLine(CharSequence line);

    void finalizeBlock(InlineParser inlineParser);

    void processInlines(InlineParser inlineParser);

}
