package org.commonmark.internal;

import org.commonmark.node.Block;
import org.commonmark.node.HorizontalRule;
import org.commonmark.node.SourcePosition;

import java.util.regex.Pattern;

public class HorizontalRuleParser extends AbstractBlockParser {

    private static Pattern H_RULE = Pattern.compile("^(?:(?:\\* *){3,}|(?:_ *){3,}|(?:- *){3,}) *$");

    private final HorizontalRule block = new HorizontalRule();

    public HorizontalRuleParser(SourcePosition pos) {
        block.setSourcePosition(pos);
    }

    @Override
    public ContinueResult tryContinue(ParserState state) {
        // a horizontal rule can never container > 1 line, so fail to match
        return blockDidNotMatch();
    }

    @Override
    public void addLine(CharSequence line) {

    }

    @Override
    public Block getBlock() {
        return block;
    }

    public static class Factory extends AbstractBlockParserFactory {

        @Override
        public StartResult tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            int offset = state.getIndex();
            int nextNonSpace = state.getNextNonSpaceIndex();
            if (nextNonSpace - offset >= 4) {
                return noStart();
            }
            CharSequence line = state.getLine();
            if (H_RULE.matcher(line.subSequence(nextNonSpace, line.length())).matches()) {
                return start(new HorizontalRuleParser(pos(state, nextNonSpace)), line.length(), false);
            } else {
                return noStart();
            }
        }
    }
}
