package org.commonmark.internal;

import org.commonmark.node.SourcePosition;

import java.util.Collections;

public abstract class AbstractBlockParserFactory implements BlockParserFactory {

    private static NoStart NO_START = new NoStart() {
    };

    protected NoStart noStart() {
        return NO_START;
    }

    protected BlockStart start(BlockParser blockParser, int newOffset, boolean replaceActiveBlockParser) {
        return new BlockStartImpl(Collections.singleton(blockParser), newOffset, replaceActiveBlockParser);
    }

    protected BlockStart start(Iterable<BlockParser> blockParsers, int newOffset, boolean replaceActiveBlockParser) {
        return new BlockStartImpl(blockParsers, newOffset, replaceActiveBlockParser);
    }

    protected SourcePosition pos(ParserState state, int columnNumber) {
        return new SourcePosition(state.getLineNumber(), columnNumber);
    }

    private static class AbstractBlockStartResult {
        private final Iterable<BlockParser> blockParsers;
        private final int newOffset;
        private final boolean replaceActiveBlockParser;

        public AbstractBlockStartResult(Iterable<BlockParser> blockParsers, int newOffset,
                                        boolean replaceActiveBlockParser) {
            this.blockParsers = blockParsers;
            this.newOffset = newOffset;
            this.replaceActiveBlockParser = replaceActiveBlockParser;
        }

        public Iterable<BlockParser> getBlockParsers() {
            return blockParsers;
        }

        public int getNewOffset() {
            return newOffset;
        }

        public boolean replaceActiveBlockParser() {
            return replaceActiveBlockParser;
        }
    }

    private static class BlockStartImpl extends AbstractBlockStartResult implements BlockStart {
        public BlockStartImpl(Iterable<BlockParser> blockParsers, int newOffset, boolean replaceActiveBlockParser) {
            super(blockParsers, newOffset, replaceActiveBlockParser);
        }
    }

}
