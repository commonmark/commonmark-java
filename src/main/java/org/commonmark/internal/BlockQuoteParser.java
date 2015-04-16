package org.commonmark.internal;

import org.commonmark.node.Block;
import org.commonmark.node.BlockQuote;
import org.commonmark.node.Node;
import org.commonmark.node.SourcePosition;

public class BlockQuoteParser extends AbstractBlockParser {

    private final BlockQuote block = new BlockQuote();

    public BlockQuoteParser(SourcePosition pos) {
        block.setSourcePosition(pos);
    }

    @Override
    public boolean canContain(Block block) {
        return true;
    }

    @Override
    public boolean shouldTryBlockStarts() {
        return true;
    }

    @Override
    public ContinueResult continueBlock(String line, int nextNonSpace, int offset, boolean blank) {
        int indent = nextNonSpace - offset;
        if (indent <= 3 && nextNonSpace < line.length() && line.charAt(nextNonSpace) == '>') {
            int newOffset = nextNonSpace + 1;
            if (newOffset < line.length() && line.charAt(newOffset) == ' ') {
                newOffset++;
            }
            return blockMatched(newOffset);
        } else {
            return blockDidNotMatch();
        }
    }

    @Override
    public BlockQuote getBlock() {
        return block;
    }

    public static class Factory extends AbstractBlockParserFactory {
        public StartResult tryStart(ParserState state) {
            String line = state.getLine();
            int nextNonSpace = state.getNextNonSpace();
            if (line.charAt(nextNonSpace) == '>') {
                int newOffset = nextNonSpace + 1;
                // optional following space
                if (newOffset < line.length() && line.charAt(newOffset) == ' ') {
                    newOffset++;
                }
                return start(new BlockQuoteParser(pos(state, nextNonSpace)), newOffset, false);
            } else {
                return noStart();
            }
        }
    }
}
