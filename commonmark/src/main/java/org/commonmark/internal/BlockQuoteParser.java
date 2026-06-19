package org.commonmark.internal;

import org.commonmark.internal.util.Parsing;
import org.commonmark.node.Block;
import org.commonmark.node.BlockQuote;
import org.commonmark.parser.block.*;
import org.commonmark.text.Characters;

public class BlockQuoteParser extends AbstractBlockParser {

    private final BlockQuote block = new BlockQuote();

    @Override
    public boolean isContainer() {
        return true;
    }

    @Override
    public boolean canContain(Block block) {
        return true;
    }

    @Override
    public BlockQuote getBlock() {
        return block;
    }

    private static int calculateNewColumn(ParserState state, int nextNonSpace) {
        int newColumn = state.getColumn() + state.getIndent() + 1;

        if (Characters.isSpaceOrTab(state.getLine().getContent(), nextNonSpace + 1)) {
            newColumn++;
        }

        return newColumn;
    }

    @Override
    public BlockContinue tryContinue(ParserState state) {
        int nextNonSpace = state.getNextNonSpaceIndex();
        if (isMarker(state, nextNonSpace)) {
            return BlockContinue.atColumn(
                    calculateNewColumn(state, nextNonSpace));
        }

        return BlockContinue.none();
        }


    private static boolean isMarker(ParserState state, int index) {
        CharSequence line = state.getLine().getContent();
        return state.getIndent() < Parsing.CODE_BLOCK_INDENT && index < line.length() && line.charAt(index) == '>';
    }



    public static class Factory extends AbstractBlockParserFactory {
        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            int nextNonSpace = state.getNextNonSpaceIndex();
            if (isMarker(state, nextNonSpace)) {
                return BlockStart.of(new BlockQuoteParser())
                        .atColumn(BlockQuoteParser.calculateNewColumn(state, nextNonSpace));
            }

            return BlockStart.none();
            }
        }
    }

