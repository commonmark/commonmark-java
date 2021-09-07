package org.commonmark.internal;

import org.commonmark.internal.util.Parsing;
import org.commonmark.node.Block;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.parser.SourceLine;
import org.commonmark.parser.block.AbstractBlockParser;
import org.commonmark.parser.block.AbstractBlockParserFactory;
import org.commonmark.parser.block.BlockContinue;
import org.commonmark.parser.block.BlockStart;
import org.commonmark.parser.block.MatchedBlockParser;
import org.commonmark.parser.block.ParserState;

public class FencedCodeBlockParser extends AbstractBlockParser {

    private final FencedCodeBlock block = new FencedCodeBlock();

    private String firstLine;
    private StringBuilder otherLines = new StringBuilder();
    private StringBuilder rawLines = new StringBuilder();

    public FencedCodeBlockParser(char fenceChar, int fenceLength, int fenceIndent) {
        block.setFenceChar(fenceChar);
        block.setStartFenceLength(fenceLength);
        
        // Fenced code blocks can't be indented by newlines or tabs, so it's safe to assume
        //    spaces for the indentation
        block.setPreStartFenceWhitespace(Parsing.generateSpaces(fenceIndent));
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public BlockContinue tryContinue(ParserState state) {
        int nextNonSpace = state.getNextNonSpaceIndex();
        int newIndex = state.getIndex();
        CharSequence line = state.getLine().getContent();
        if (state.getIndent() < Parsing.CODE_BLOCK_INDENT && nextNonSpace < line.length() && line.charAt(nextNonSpace) == block.getFenceChar() && isClosing(line, nextNonSpace)) {
            // Capture whitespace before closing fence (if any) for roundtrip purposes
            if(nextNonSpace > 0) {
                block.setPreEndFenceWhitespace(Parsing.collectWhitespace(line, 0, nextNonSpace));
            }
            
            // closing fence - we're at end of line, so we can finalize now
            return BlockContinue.finished();
        } else {
            // Capture line before optional spaces are removed
            rawLines.append(line.toString());
            rawLines.append('\n');
            
            // skip optional spaces of fence indent
            int i = block.getStartFenceIndent();
            int length = line.length();
            while (i > 0 && newIndex < length && line.charAt(newIndex) == ' ') {
                newIndex++;
                i--;
            }
        }
        return BlockContinue.atIndex(newIndex);
    }

    @Override
    public void addLine(SourceLine line) {
        if (firstLine == null) {

            // Raw line is the same as literal line, so just use it
            if(line.getLiteralIndex() == 0) {
                firstLine = line.getContent().toString();
            }
            // Raw line differs from literal line, so preserve literal line
            else {
                firstLine = line.substring(line.getLiteralIndex(), line.getContent().length()).getContent().toString();
            }
        } else {
            // Raw line is the same as literal line, so just use it
            if(line.getLiteralIndex() == 0) {
                otherLines.append(line.getContent());
            }
            // Raw line differs from literal line, so preserve literal line
            else {
                otherLines.append(line.getLiteralLine().getContent());
            }
            otherLines.append('\n');
        }
    }

    @Override
    public void closeBlock() {
        // first line becomes info string
        // It is not trimmed or escaped here to allow roundtrip processing
        block.setInfo(firstLine);
        
        // Remove final newline in raw content, if it exists
        if(rawLines != null && rawLines.length() != 0) {
            if(rawLines.charAt(rawLines.length() - 1) == '\n') {
                rawLines.deleteCharAt(rawLines.length() - 1);
            }
        }
        
        // Capture raw content (no indentation removed) between end of info string
        //    and beginning of end fence
        block.setRaw(rawLines.toString());
        
        // Capture content (without indentation) between end of info string
        //    and beginning of end fence
        block.setLiteral(otherLines.toString());
    }

    public static class Factory extends AbstractBlockParserFactory {

        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            int indent = state.getIndent();
            if (indent >= Parsing.CODE_BLOCK_INDENT) {
                return BlockStart.none();
            }

            if(indent == 0) {
                indent = Parsing.collectWhitespace(state.getLine().getContent(), 0, state.getLine().getContent().length()).length();
            }
            
            int nextNonSpace = state.getNextNonSpaceIndex();
            FencedCodeBlockParser blockParser = checkOpener(state.getLine().getContent(), nextNonSpace, indent);
            if (blockParser != null) {
                return BlockStart.of(blockParser).atIndex(nextNonSpace + blockParser.block.getStartFenceLength());
            } else {
                return BlockStart.none();
            }
        }
    }

    // spec: A code fence is a sequence of at least three consecutive backtick characters (`) or tildes (~). (Tildes and
    // backticks cannot be mixed.)
    private static FencedCodeBlockParser checkOpener(CharSequence line, int index, int indent) {
        int backticks = 0;
        int tildes = 0;
        int length = line.length();
        loop:
        for (int i = index; i < length; i++) {
            switch (line.charAt(i)) {
                case '`':
                    backticks++;
                    break;
                case '~':
                    tildes++;
                    break;
                default:
                    break loop;
            }
        }
        if (backticks >= 3 && tildes == 0) {
            // spec: If the info string comes after a backtick fence, it may not contain any backtick characters.
            if (Parsing.find('`', line, index + backticks) != -1) {
                return null;
            }
            return new FencedCodeBlockParser('`', backticks, indent);
        } else if (tildes >= 3 && backticks == 0) {
            // spec: Info strings for tilde code blocks can contain backticks and tildes
            return new FencedCodeBlockParser('~', tildes, indent);
        } else {
            return null;
        }
    }

    // spec: The content of the code block consists of all subsequent lines, until a closing code fence of the same type
    // as the code block began with (backticks or tildes), and with at least as many backticks or tildes as the opening
    // code fence.
    private boolean isClosing(CharSequence line, int index) {
        char fenceChar = block.getFenceChar();
        int fenceLength = block.getStartFenceLength();
        int fences = Parsing.skip(fenceChar, line, index, line.length()) - index;
        if (fences < fenceLength) {
            return false;
        }else {
            block.setEndFenceLength(fences);
        }
        
        // spec: The closing code fence [...] may be followed only by spaces, which are ignored.
        int after = Parsing.skipSpaceTab(line, index + fences, line.length());
        
        // Capture post-fence spaces (if any) for roundtrip purposes
        if(after > 0 && after != (index + fences)) {
            block.setPostBlockWhitespace(Parsing.collectWhitespaceBackwards(line, line.length() - 1, 0));
        }
        
        return after == line.length();
    }
}
