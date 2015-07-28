package org.commonmark.internal;

import org.commonmark.node.*;
import org.commonmark.parser.block.*;

import java.util.regex.Pattern;

public class IndentedCodeBlockParser extends AbstractBlockParser {

    public static int INDENT = 4;

    private static final Pattern TRAILING_BLANK_LINES = Pattern.compile("(?:\n[ \t]*)+$");

    private final IndentedCodeBlock block = new IndentedCodeBlock();
    private BlockContent content = new BlockContent();

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public BlockContinue tryContinue(ParserState state) {
        if (state.getIndent() >= INDENT) {
            return BlockContinue.atColumn(state.getColumn() + INDENT);
        } else if (state.isBlank()) {
            return BlockContinue.atIndex(state.getNextNonSpaceIndex());
        } else {
            return BlockContinue.none();
        }
    }

    @Override
    public void addLine(CharSequence line) {
        content.add(line);
    }

    @Override
    public void closeBlock() {
        // add trailing newline
        content.add("");
        String contentString = content.getString();
        content = null;

        String literal = TRAILING_BLANK_LINES.matcher(contentString).replaceFirst("\n");
        block.setLiteral(literal);
    }

    public static class Factory extends AbstractBlockParserFactory {

        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            // An indented code block cannot interrupt a paragraph.
            if (state.getIndent() >= INDENT && !state.isBlank() && !(state.getActiveBlockParser().getBlock() instanceof Paragraph)) {
                int nextNonSpace = state.getNextNonSpaceIndex();
                return BlockStart.of(new IndentedCodeBlockParser()).atColumn(state.getColumn() + INDENT);
            } else {
                return BlockStart.none();
            }
        }
    }
}

