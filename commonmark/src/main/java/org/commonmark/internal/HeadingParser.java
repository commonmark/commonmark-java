package org.commonmark.internal;

import org.commonmark.internal.util.Parsing;
import org.commonmark.node.Block;
import org.commonmark.node.Heading;
import org.commonmark.parser.InlineParser;
import org.commonmark.parser.block.*;

public class HeadingParser extends AbstractBlockParser {

    private final Heading block = new Heading();
    private final String content;

    public HeadingParser(int level, String content) {
        block.setLevel(level);
        this.content = content;
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public BlockContinue tryContinue(ParserState parserState) {
        // In both ATX and Setext headings, once we have the heading markup, there's nothing more to parse.
        return BlockContinue.none();
    }

    @Override
    public void parseInlines(InlineParser inlineParser) {
        inlineParser.parse(content, block);
    }

    public static class Factory extends AbstractBlockParserFactory {

        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            if (state.getIndent() >= Parsing.CODE_BLOCK_INDENT) {
                return BlockStart.none();
            }

            CharSequence line = state.getLine();
            int nextNonSpace = state.getNextNonSpaceIndex();
            HeadingParser atxHeading = getAtxHeading(line, nextNonSpace);
            if (atxHeading != null) {
                return BlockStart.of(atxHeading).atIndex(line.length());
            }

            int setextHeadingLevel = getSetextHeadingLevel(line, nextNonSpace);
            if (setextHeadingLevel > 0) {
                CharSequence paragraph = matchedBlockParser.getParagraphContent();
                if (paragraph != null) {
                    String content = paragraph.toString();
                    return BlockStart.of(new HeadingParser(setextHeadingLevel, content))
                            .atIndex(line.length())
                            .replaceActiveBlockParser();
                }
            }

            return BlockStart.none();
        }
    }

    // spec: An ATX heading consists of a string of characters, parsed as inline content, between an opening sequence of
    // 1–6 unescaped # characters and an optional closing sequence of any number of unescaped # characters. The opening
    // sequence of # characters must be followed by a space or by the end of line. The optional closing sequence of #s
    // must be preceded by a space and may be followed by spaces only.
    private static HeadingParser getAtxHeading(CharSequence line, int index) {
        int level = Parsing.skip('#', line, index, line.length()) - index;

        if (level == 0 || level > 6) {
            return null;
        }

        int start = index + level;
        if (start >= line.length()) {
            // End of line after markers is an empty heading
            return new HeadingParser(level, "");
        }

        char next = line.charAt(start);
        if (!(next == ' ' || next == '\t')) {
            return null;
        }

        int beforeSpace = Parsing.skipSpaceTabBackwards(line, line.length() - 1, start);
        int beforeHash = Parsing.skipBackwards('#', line, beforeSpace, start);
        int beforeTrailer = Parsing.skipSpaceTabBackwards(line, beforeHash, start);
        if (beforeTrailer != beforeHash) {
            return new HeadingParser(level, line.subSequence(start, beforeTrailer + 1).toString());
        } else {
            return new HeadingParser(level, line.subSequence(start, beforeSpace + 1).toString());
        }
    }

    // spec: A setext heading underline is a sequence of = characters or a sequence of - characters, with no more than
    // 3 spaces indentation and any number of trailing spaces.
    private static int getSetextHeadingLevel(CharSequence line, int index) {
        switch (line.charAt(index)) {
            case '=':
                if (isSetextHeadingRest(line, index + 1, '=')) {
                    return 1;
                }
            case '-':
                if (isSetextHeadingRest(line, index + 1, '-')) {
                    return 2;
                }
        }
        return 0;
    }

    private static boolean isSetextHeadingRest(CharSequence line, int index, char marker) {
        int afterMarker = Parsing.skip(marker, line, index, line.length());
        int afterSpace = Parsing.skipSpaceTab(line, afterMarker, line.length());
        return afterSpace >= line.length();
    }
}
