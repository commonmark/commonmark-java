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
    public ContinueResult continueBlock(String line, int nextNonSpace, int offset, boolean blank) {
        // a horizontal rule can never container > 1 line, so fail to match
        return blockDidNotMatch();
    }

    @Override
    public void addLine(String line) {

    }

    @Override
    public Block getBlock() {
        return block;
    }

    public static class Factory extends AbstractBlockParserFactory {

        @Override
        public StartResult tryStart(ParserState state) {
            int nextNonSpace = state.getNextNonSpace();
            if (H_RULE.matcher(state.getLine().substring(nextNonSpace)).matches()) {
                return start(new HorizontalRuleParser(pos(state, nextNonSpace)), state.getLine().length(), false);
            } else {
                return noStart();
            }
        }
    }
}
