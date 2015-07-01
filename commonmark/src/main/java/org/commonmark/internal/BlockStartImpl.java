package org.commonmark.internal;

import org.commonmark.parser.block.BlockParser;
import org.commonmark.parser.block.BlockStart;

public class BlockStartImpl extends BlockStart {

    private final Iterable<BlockParser> blockParsers;
    private final int newIndex;
    private final boolean replaceActiveBlockParser;

    public BlockStartImpl(Iterable<BlockParser> blockParsers, int newIndex,
                                    boolean replaceActiveBlockParser) {
        this.blockParsers = blockParsers;
        this.newIndex = newIndex;
        this.replaceActiveBlockParser = replaceActiveBlockParser;
    }

    public Iterable<BlockParser> getBlockParsers() {
        return blockParsers;
    }

    public int getNewIndex() {
        return newIndex;
    }

    public boolean replaceActiveBlockParser() {
        return replaceActiveBlockParser;
    }

}
