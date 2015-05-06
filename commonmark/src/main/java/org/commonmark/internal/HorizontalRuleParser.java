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
    public ContinueResult continueBlock(CharSequence line, int nextNonSpace, int offset, boolean blank) {
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
        public StartResult tryStart(ParserState state) {
            int nextNonSpace = state.getNextNonSpace();
            CharSequence line = state.getLine();
            if (H_RULE.matcher(line.subSequence(nextNonSpace, line.length())).matches()) {
                return start(new HorizontalRuleParser(pos(state, nextNonSpace)), line.length(), false);
            } else {
                return noStart();
            }
        }
    }
}
