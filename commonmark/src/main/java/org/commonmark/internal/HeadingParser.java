package org.commonmark.internal;

import org.commonmark.internal.inline.Position;
import org.commonmark.internal.inline.Scanner;
import org.commonmark.internal.util.Parsing;
import org.commonmark.node.Block;
import org.commonmark.node.Heading;
import org.commonmark.parser.InlineParser;
import org.commonmark.parser.SourceLine;
import org.commonmark.parser.SourceLines;
import org.commonmark.parser.block.AbstractBlockParser;
import org.commonmark.parser.block.AbstractBlockParserFactory;
import org.commonmark.parser.block.BlockContinue;
import org.commonmark.parser.block.BlockStart;
import org.commonmark.parser.block.MatchedBlockParser;
import org.commonmark.parser.block.ParserState;

public class HeadingParser extends AbstractBlockParser {

    private final Heading block = new Heading();
    private final SourceLines content;

    public HeadingParser(int level, SourceLines content) {
    	block.setLevel(level);
        block.setSymbolType('#');
        this.content = content;
    }
    
    public HeadingParser(int level, SourceLines content, char symbolType, int numEndingSymbols, String... whitespace) {
        this(level, content);
        block.setSymbolType(symbolType);
        block.setNumEndingSymbol(numEndingSymbols);
        
        if(whitespace.length == 4) {
            block.setWhitespace(whitespace);
        }else {
            String[] tempArray = {"", "", "", ""};
            
            for(int i = 0; i < whitespace.length; i++) {
                tempArray[i] = whitespace[i];
            }
            
            block.setWhitespace(tempArray);
        }
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

            String preBlockWhitespace = "";
            
            if (line.getContent().charAt(nextNonSpace) == '#') {
                
                // Account for block-in-a-block instances during roundtrips, like Heading inside a List
                if(nextNonSpace > 0) {
                    preBlockWhitespace = Parsing.collectWhitespaceBackwards(line.getContent(), nextNonSpace - 1, 0);
                    
                    // If these values don't match, it's a block-in-a-block scenario because
                    //    there's another character before the whitespace
                    if(preBlockWhitespace.length() != nextNonSpace) {
                        preBlockWhitespace = "";
                    }
                }

                HeadingParser atxHeading = getAtxHeading(line.substring(nextNonSpace - preBlockWhitespace.length(), line.getContent().length()), nextNonSpace);
                
                if (atxHeading != null) {
                    return BlockStart.of(atxHeading).atIndex(line.getContent().length());
                }
            }

            int setextHeadingLevel = getSetextHeadingLevel(line.getContent(), nextNonSpace);
            if (setextHeadingLevel > 0) {
                int numEndingSymbols = line.getContent().toString().trim().length();
                
                // AST: Setext headings have a slightly different twist on whitespace:
                //      Pre-block = Before setext text begins
                //      Pre-content = Directly after setext text
                //      Post-content = Before setext delimiting line
                //      Post-block = After setext delimiting line
                String preContentWhitespace = "";
                String postContentWhitespace = Parsing.collectWhitespace(line.getContent(), 0, line.getContent().length());
                String postBlockWhitespace = Parsing.collectWhitespaceBackwards(line.getContent(), line.getContent().length() - 1, 0);
                
                // If the setext had valid content, the current matched block
                //    parser will be the paragraph parser. Setext headings use
                //    Text (not Paragraph) in the final output, but it's still
                //    possible to gather the right whitespace from Paragraph.
                boolean isMatchedParagraph = matchedBlockParser.getMatchedBlockParser() instanceof ParagraphParser;
                
                if(isMatchedParagraph) {
                    SourceLines rawLines = ((ParagraphParser)matchedBlockParser.getMatchedBlockParser()).getRawParagraphLines();
                    
                    // In some instances, the paragraph object exists but has not yet been populated.
                    //    However, the link reference definition parser will still have the paragraph's
                    //    lines ready for capture. If no whitespace is available yet, capture any initial
                    //    whitespace in the first paragraph line.
                    if(preBlockWhitespace.isEmpty() && !rawLines.isEmpty()) {
                        CharSequence rawParagraphSequence = rawLines.getLines().get(0).getContent();
                        preBlockWhitespace = Parsing.collectWhitespace(rawParagraphSequence.toString(), 0, rawParagraphSequence.length());
                    }
                }
                
                char symbolType;
                if(setextHeadingLevel == 1) {
                    symbolType = '=';
                }else {
                    symbolType = '-';
                }
                
                SourceLines paragraph = matchedBlockParser.getParagraphLines();
                if (!paragraph.isEmpty()) {
                    return BlockStart.of(new HeadingParser(setextHeadingLevel, paragraph, symbolType, numEndingSymbols, preBlockWhitespace, preContentWhitespace, postContentWhitespace, postBlockWhitespace))
                            .atIndex(line.getContent().length())
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
    private static HeadingParser getAtxHeading(SourceLine line) {
        return getAtxHeading(line, 0);
    }
    
    private static HeadingParser getAtxHeading(SourceLine line, int nextNonSpace) {
        String preBlockWhitespace = "";
        
        Scanner scanner = Scanner.of(SourceLines.of(line));
        preBlockWhitespace = scanner.whitespaceAsString();
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

        String preContentWhitespace = scanner.whitespaceAsString();
        Position start = scanner.position();
        Position end = start;
        boolean hashCanEnd = true;
        int numEndingSymbols = 0;
        StringBuilder postContentWhitespace = new StringBuilder();
        String postBlockWhitespace = "";

        while (scanner.hasNext()) {
            char c = scanner.peek();
            switch (c) {
                case '#':
                    if (hashCanEnd) {
                        numEndingSymbols = scanner.matchMultiple('#');
                        String whitespace = scanner.whitespaceAsString();
                        // If there's other characters, the hashes and spaces were part of the heading
                        if (scanner.hasNext()) {
                            numEndingSymbols = 0;
                            postContentWhitespace.setLength(0);
                            end = scanner.position();
                        }
                        hashCanEnd = whitespace.length() > 0;
                    } else {
                        scanner.next();
                        end = scanner.position();
                    }
                    break;
                case ' ':
                case '\t':
                    hashCanEnd = true;
                    postContentWhitespace.append(c);
                    scanner.next();
                    break;
                default:
                    hashCanEnd = false;
                    postContentWhitespace.setLength(0);
                    scanner.next();
                    end = scanner.position();
            }
        }
        
        if(numEndingSymbols > 0) {
            int beforePostBlockWhitespaceIndex = Parsing.skipSpaceTabBackwards(line.getContent(), line.getContent().length() - 1, 0);
            postBlockWhitespace = line.getContent().subSequence(beforePostBlockWhitespaceIndex + 1, line.getContent().length()).toString();
        }

        SourceLines source = scanner.getSource(start, end);
        String content = source.getContent();
        
        if(content.isEmpty()) {
            return new HeadingParser(level, SourceLines.empty(), '#', numEndingSymbols, preBlockWhitespace, preContentWhitespace, postContentWhitespace.toString(), postBlockWhitespace);
        }
        return new HeadingParser(level, source, '#', numEndingSymbols, preBlockWhitespace, preContentWhitespace, postContentWhitespace.toString(), postBlockWhitespace);
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
