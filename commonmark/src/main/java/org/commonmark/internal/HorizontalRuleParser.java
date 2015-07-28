package org.commonmark.internal;

import org.commonmark.node.Block;
import org.commonmark.node.HorizontalRule;
import org.commonmark.parser.block.*;

import java.util.regex.Pattern;

public class HorizontalRuleParser extends AbstractBlockParser {

    private static Pattern H_RULE = Pattern.compile("^(?:(?:\\* *){3,}|(?:_ *){3,}|(?:- *){3,}) *$");

    private final HorizontalRule block = new HorizontalRule();

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
            if (H_RULE.matcher(line.subSequence(nextNonSpace, line.length())).matches()) {
                return BlockStart.of(new HorizontalRuleParser()).atIndex(line.length());
            } else {
                return BlockStart.none();
            }
        }
    }
}
