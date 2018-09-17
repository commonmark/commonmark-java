package org.commonmark.internal;

import org.commonmark.internal.util.Parsing;
import org.commonmark.node.Block;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.parser.block.*;

import static org.commonmark.internal.util.Escaping.unescapeString;

public class FencedCodeBlockParser extends AbstractBlockParser {

    private final FencedCodeBlock block = new FencedCodeBlock();

    private String firstLine;
    private StringBuilder otherLines = new StringBuilder();

    public FencedCodeBlockParser(char fenceChar, int fenceLength, int fenceIndent) {
        block.setFenceChar(fenceChar);
        block.setFenceLength(fenceLength);
        block.setFenceIndent(fenceIndent);
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public BlockContinue tryContinue(ParserState state) {
        int nextNonSpace = state.getNextNonSpaceIndex();
        int newIndex = state.getIndex();
        CharSequence line = state.getLine();
        boolean closing = state.getIndent() < Parsing.CODE_BLOCK_INDENT && isClosing(line, nextNonSpace);
        if (closing) {
            // closing fence - we're at end of line, so we can finalize now
            return BlockContinue.finished();
        } else {
            // skip optional spaces of fence indent
            int i = block.getFenceIndent();
            while (i > 0 && newIndex < line.length() && line.charAt(newIndex) == ' ') {
                newIndex++;
                i--;
            }
        }
        return BlockContinue.atIndex(newIndex);
    }

    @Override
    public void addLine(CharSequence line) {
        if (firstLine == null) {
            firstLine = line.toString();
        } else {
            otherLines.append(line);
            otherLines.append('\n');
        }
    }

    @Override
    public void closeBlock() {
        // first line becomes info string
        block.setInfo(unescapeString(firstLine.trim()));
        block.setLiteral(otherLines.toString());
    }

    public static class Factory extends AbstractBlockParserFactory {

        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            int indent = state.getIndent();
            if (indent >= Parsing.CODE_BLOCK_INDENT) {
                return BlockStart.none();
            }

            int nextNonSpace = state.getNextNonSpaceIndex();
            FencedCodeBlockParser blockParser = checkOpener(state.getLine(), nextNonSpace, indent);
            if (blockParser != null) {
                return BlockStart.of(blockParser).atIndex(nextNonSpace + blockParser.block.getFenceLength());
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
        loop:
        for (int i = index; i < line.length(); i++) {
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
            // spec: The info string may not contain any backtick characters.
            if (Parsing.find('`', line, index + backticks) != -1) {
                return null;
            }
            return new FencedCodeBlockParser('`', backticks, indent);
        } else if (tildes >= 3 && backticks == 0) {
            // This follows commonmark.js but the spec is unclear about this:
            // https://github.com/commonmark/CommonMark/issues/119
            if (Parsing.find('~', line, index + tildes) != -1) {
                return null;
            }
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
        int fenceLength = block.getFenceLength();
        int fences = Parsing.skip(fenceChar, line, index, line.length()) - index;
        if (fences < fenceLength) {
            return false;
        }
        // spec: The closing code fence [...] may be followed only by spaces, which are ignored.
        int after = Parsing.skipSpaceTab(line, index + fences, line.length());
        return after == line.length();
    }
}
