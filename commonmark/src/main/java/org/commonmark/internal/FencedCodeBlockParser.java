package org.commonmark.internal;

import org.commonmark.node.Block;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.SourcePosition;
import org.commonmark.parser.BlockContinue;
import org.commonmark.parser.BlockStart;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.commonmark.internal.util.Escaping.unescapeString;

public class FencedCodeBlockParser extends AbstractBlockParser {

    private static final Pattern OPENING_FENCE = Pattern.compile("^`{3,}(?!.*`)|^~{3,}(?!.*~)");
    private static final Pattern CLOSING_FENCE = Pattern.compile("^(?:`{3,}|~{3,})(?= *$)");

    private final FencedCodeBlock block = new FencedCodeBlock();
    private BlockContent content = new BlockContent();

    public FencedCodeBlockParser(char fenceChar, int fenceLength, int fenceIndent, SourcePosition pos) {
        block.setFenceChar(fenceChar);
        block.setFenceLength(fenceLength);
        block.setFenceIndent(fenceIndent);
        block.setSourcePosition(pos);
    }

    @Override
    public BlockContinue tryContinue(ParserState state) {
        int nextNonSpace = state.getNextNonSpaceIndex();
        int indent = nextNonSpace - state.getIndex();
        int newIndex = state.getIndex();
        CharSequence line = state.getLine();
        Matcher matcher = null;
        boolean matches = (indent <= 3 &&
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
        return BlockContinue.of(newIndex);
    }

    @Override
    public void addLine(CharSequence line) {
        content.add(line);
    }

    @Override
    public void finalizeBlock(InlineParser inlineParser) {
        boolean singleLine = content.hasSingleLine();
        // add trailing newline
        content.add("");
        String contentString = content.getString();
        content = null;

        // first line becomes info string
        int firstNewline = contentString.indexOf('\n');
        String firstLine = contentString.substring(0, firstNewline);
        block.setInfo(unescapeString(firstLine.trim()));
        if (singleLine) {
            block.setLiteral("");
        } else {
            String literal = contentString.substring(firstNewline + 1);
            block.setLiteral(literal);
        }
    }

    @Override
    public Block getBlock() {
        return block;
    }

    public static class Factory extends AbstractBlockParserFactory {

        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            int nextNonSpace = state.getNextNonSpaceIndex();
            Matcher matcher;
            CharSequence line = state.getLine();
            if ((matcher = OPENING_FENCE.matcher(line.subSequence(nextNonSpace, line.length()))).find()) {
                int fenceLength = matcher.group(0).length();
                char fenceChar = matcher.group(0).charAt(0);
                int indent = nextNonSpace - state.getIndex();
                FencedCodeBlockParser blockParser = new FencedCodeBlockParser(fenceChar, fenceLength, indent, pos(state, nextNonSpace));
                return BlockStart.of(blockParser, nextNonSpace + fenceLength);
            } else {
                return BlockStart.none();
            }
        }
    }
}

