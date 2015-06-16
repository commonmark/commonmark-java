package org.commonmark.internal;

import org.commonmark.node.Block;
import org.commonmark.node.Header;
import org.commonmark.node.SourcePosition;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HeaderParser extends AbstractBlockParser {

    private static Pattern ATX_HEADER = Pattern.compile("^#{1,6}(?: +|$)");
    private static Pattern ATX_TRAILING = Pattern.compile("(^| ) *#+ *$");
    private static Pattern SETEXT_HEADER = Pattern.compile("^(?:=+|-+) *$");

    private final Header block = new Header();
    private final String content;

    public HeaderParser(int level, String content, SourcePosition pos) {
        block.setLevel(level);
        block.setSourcePosition(pos);
        this.content = content;
    }

    @Override
    public ContinueResult continueBlock(CharSequence line, int nextNonSpace, int offset, boolean blank) {
        // a header can never container > 1 line, so fail to match
        return blockDidNotMatch();
    }

    @Override
    public void processInlines(InlineParser inlineParser) {
        inlineParser.parse(block, content);
    }

    @Override
    public Block getBlock() {
        return block;
    }

    public static class Factory extends AbstractBlockParserFactory {

        @Override
        public StartResult tryStart(ParserState state) {
            if (state.isIndented()) {
                return noStart();
            }
            CharSequence line = state.getLine();
            int nextNonSpace = state.getNextNonSpace();
            CharSequence paragraphStartLine = state.getParagraphStartLine();
            Matcher matcher;
            if ((matcher = ATX_HEADER.matcher(line.subSequence(nextNonSpace, line.length()))).find()) {
                // ATX header
                int newOffset = nextNonSpace + matcher.group(0).length();
                int level = matcher.group(0).trim().length(); // number of #s
                // remove trailing ###s:
                String content = ATX_TRAILING.matcher(line.subSequence(newOffset, line.length())).replaceAll("");
                return start(new HeaderParser(level, content, pos(state, nextNonSpace)), line.length(), false);

            } else if (paragraphStartLine != null &&
                    ((matcher = SETEXT_HEADER.matcher(line.subSequence(nextNonSpace, line.length()))).find())) {
                // setext header line

                int level = matcher.group(0).charAt(0) == '=' ? 1 : 2;
                String content = paragraphStartLine.toString();
                return start(new HeaderParser(level, content, state.getActiveBlockParser().getBlock().getSourcePosition()), line.length(), true);
            } else {
                return noStart();
            }
        }
    }
}
