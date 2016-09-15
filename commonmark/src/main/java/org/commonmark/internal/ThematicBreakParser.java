package org.commonmark.internal;

import org.commonmark.node.Block;
import org.commonmark.node.ThematicBreak;
import org.commonmark.parser.block.*;

import java.util.regex.Pattern;

public class ThematicBreakParser extends AbstractBlockParser {

    private static Pattern PATTERN = Pattern.compile("^(?:(?:\\*[ \t]*){3,}|(?:_[ \t]*){3,}|(?:-[ \t]*){3,})[ \t]*$");

    private final ThematicBreak block = new ThematicBreak();

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public BlockContinue tryContinue(ParserState state) {
        // a horizontal rule can never container > 1 line, so fail to match
        return BlockContinue.none();
    }

    public static class Factory extends AbstractBlockParserFactory {

        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            if (state.getIndent() >= 4) {
                return BlockStart.none();
            }
            int nextNonSpace = state.getNextNonSpaceIndex();
            CharSequence line = state.getLine();
            if (PATTERN.matcher(line.subSequence(nextNonSpace, line.length())).matches()) {
                return BlockStart.of(new ThematicBreakParser()).atIndex(line.length());
            } else {
                return BlockStart.none();
            }
        }
    }
}
