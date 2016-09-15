package org.commonmark.internal;

import org.commonmark.node.Block;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.parser.block.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.commonmark.internal.util.Escaping.unescapeString;

public class FencedCodeBlockParser extends AbstractBlockParser {

    private static final Pattern OPENING_FENCE = Pattern.compile("^`{3,}(?!.*`)|^~{3,}(?!.*~)");
    private static final Pattern CLOSING_FENCE = Pattern.compile("^(?:`{3,}|~{3,})(?= *$)");

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
        Matcher matcher = null;
        boolean matches = (state.getIndent() <= 3 &&
                nextNonSpace < line.length() &&
                line.charAt(nextNonSpace) == block.getFenceChar() &&
                (matcher = CLOSING_FENCE.matcher(line.subSequence(nextNonSpace, line.length())))
                        .find());
        if (matches && matcher.group(0).length() >= block.getFenceLength()) {
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
            int nextNonSpace = state.getNextNonSpaceIndex();
            CharSequence line = state.getLine();
            Matcher matcher;
            if (state.getIndent() < 4 && (matcher = OPENING_FENCE.matcher(line.subSequence(nextNonSpace, line.length()))).find()) {
                int fenceLength = matcher.group(0).length();
                char fenceChar = matcher.group(0).charAt(0);
                FencedCodeBlockParser blockParser = new FencedCodeBlockParser(fenceChar, fenceLength, state.getIndent());
                return BlockStart.of(blockParser).atIndex(nextNonSpace + fenceLength);
            } else {
                return BlockStart.none();
            }
        }
    }
}

