package org.commonmark.internal;

import org.commonmark.internal.util.Parsing;
import org.commonmark.node.Block;
import org.commonmark.node.Heading;
import org.commonmark.parser.InlineParser;
import org.commonmark.parser.SourceLine;
import org.commonmark.parser.SourceLines;
import org.commonmark.parser.beta.Position;
import org.commonmark.parser.beta.Scanner;
import org.commonmark.parser.block.*;
import org.commonmark.text.Characters;

public class HeadingParser extends AbstractBlockParser {

    private final Heading block = new Heading();
    private final SourceLines content;

    public HeadingParser(int level, SourceLines content) {
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

            SourceLine line = state.getLine();
            int nextNonSpace = state.getNextNonSpaceIndex();
            if (line.getContent().charAt(nextNonSpace) == '#') {
                HeadingParser atxHeading = getAtxHeading(line.substring(nextNonSpace, line.getContent().length()));
                if (atxHeading != null) {
                    return BlockStart.of(atxHeading).atIndex(line.getContent().length());
                }
            }

            int setextHeadingLevel = getSetextHeadingLevel(line.getContent(), nextNonSpace);
            if (setextHeadingLevel > 0) {
                SourceLines paragraph = matchedBlockParser.getParagraphLines();
                if (!paragraph.isEmpty()) {
                    return BlockStart.of(new HeadingParser(setextHeadingLevel, paragraph))
                            .atIndex(line.getContent().length())
                            .replaceParagraphLines(paragraph.getLines().size());
                }
            }

            return BlockStart.none();
        }
    }

    // spec: An ATX heading consists of a string of characters, parsed as inline content, between an opening sequence of
    // 1-6 unescaped # characters and an optional closing sequence of any number of unescaped # characters. The opening
    // sequence of # characters must be followed by a space or by the end of line. The optional closing sequence of #s
    // must be preceded by a space and may be followed by spaces only.
    private static HeadingParser getAtxHeading(SourceLine line) {
        Scanner scanner = Scanner.of(SourceLines.of(line));
        int level = scanner.matchMultiple('#');

        if (level == 0 || level > 6) {
            return null;
        }

        if (!scanner.hasNext()) {
            // End of line after markers is an empty heading
            return new HeadingParser(level, SourceLines.empty());
        }

        char next = scanner.peek();
        if (!(next == ' ' || next == '\t')) {
            return null;
        }

        scanner.whitespace();
        Position start = scanner.position();
        Position end = start;
        boolean hashCanEnd = true;

        while (scanner.hasNext()) {
            char c = scanner.peek();
            switch (c) {
                case '#':
                    if (hashCanEnd) {
                        scanner.matchMultiple('#');
                        int whitespace = scanner.whitespace();
                        // If there's other characters, the hashes and spaces were part of the heading
                        if (scanner.hasNext()) {
                            end = scanner.position();
                        }
                        hashCanEnd = whitespace > 0;
                    } else {
                        scanner.next();
                        end = scanner.position();
                    }
                    break;
                case ' ':
                case '\t':
                    hashCanEnd = true;
                    scanner.next();
                    break;
                default:
                    hashCanEnd = false;
                    scanner.next();
                    end = scanner.position();
            }
        }

        SourceLines source = scanner.getSource(start, end);
        String content = source.getContent();
        if (content.isEmpty()) {
            return new HeadingParser(level, SourceLines.empty());
        }
        return new HeadingParser(level, source);
    }

    // spec: A setext heading underline is a sequence of = characters or a sequence of - characters, with no more than
    // 3 spaces indentation and any number of trailing spaces.
    private static int getSetextHeadingLevel(CharSequence line, int index) {
        switch (line.charAt(index)) {
            case '=':
                if (isSetextHeadingRest(line, index + 1, '=')) {
                    return 1;
                }
                break;
            case '-':
                if (isSetextHeadingRest(line, index + 1, '-')) {
                    return 2;
                }
                break;
        }
        return 0;
    }

    private static boolean isSetextHeadingRest(CharSequence line, int index, char marker) {
        int afterMarker = Characters.skip(marker, line, index, line.length());
        int afterSpace = Characters.skipSpaceTab(line, afterMarker, line.length());
        return afterSpace >= line.length();
    }
}
