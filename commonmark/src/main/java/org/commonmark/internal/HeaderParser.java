package org.commonmark.internal;

import org.commonmark.node.Block;
import org.commonmark.node.Header;
import org.commonmark.parser.InlineParser;
import org.commonmark.parser.block.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HeaderParser extends AbstractBlockParser {

    private static Pattern ATX_HEADER = Pattern.compile("^#{1,6}(?: +|$)");
    private static Pattern ATX_TRAILING = Pattern.compile("(^| ) *#+ *$");
    private static Pattern SETEXT_HEADER = Pattern.compile("^(?:=+|-+) *$");

    private final Header block = new Header();
    private final String content;

    public HeaderParser(int level, String content) {
        block.setLevel(level);
        this.content = content;
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public BlockContinue tryContinue(ParserState parserState) {
        // a header can never container > 1 line, so fail to match
        return BlockContinue.none();
    }

    @Override
    public void parseInlines(InlineParser inlineParser) {
        inlineParser.parse(content, block);
    }

    public static class Factory extends AbstractBlockParserFactory {

        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            if (state.getIndent() >= 4) {
                return BlockStart.none();
            }
            CharSequence line = state.getLine();
            int nextNonSpace = state.getNextNonSpaceIndex();
            CharSequence paragraphStartLine = matchedBlockParser.getParagraphStartLine();
            Matcher matcher;
            if ((matcher = ATX_HEADER.matcher(line.subSequence(nextNonSpace, line.length()))).find()) {
                // ATX header
                int newOffset = nextNonSpace + matcher.group(0).length();
                int level = matcher.group(0).trim().length(); // number of #s
                // remove trailing ###s:
                String content = ATX_TRAILING.matcher(line.subSequence(newOffset, line.length())).replaceAll("");
                return BlockStart.of(new HeaderParser(level, content))
                        .atIndex(line.length());

            } else if (paragraphStartLine != null &&
                    ((matcher = SETEXT_HEADER.matcher(line.subSequence(nextNonSpace, line.length()))).find())) {
                // setext header line

                int level = matcher.group(0).charAt(0) == '=' ? 1 : 2;
                String content = paragraphStartLine.toString();
                return BlockStart.of(new HeaderParser(level, content))
                        .atIndex(line.length())
                        .replaceActiveBlockParser();
            } else {
                return BlockStart.none();
            }
        }
    }
}
