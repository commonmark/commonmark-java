package org.commonmark.internal;

import org.commonmark.node.*;

import java.util.regex.Pattern;

public class IndentedCodeBlockParser extends AbstractBlockParser {

    public static int INDENT = 4;

    private static final Pattern TRAILING_BLANK_LINES = Pattern.compile("(?:\n[ \t]*)+$");

    private final IndentedCodeBlock block = new IndentedCodeBlock();
    private BlockContent content = new BlockContent();

    public IndentedCodeBlockParser(SourcePosition pos) {
        block.setSourcePosition(pos);
    }

    @Override
    public ContinueResult tryContinue(ParserState state) {
        int indent = state.getNextNonSpaceIndex() - state.getIndex();
        int newIndex = state.getIndex();
        if (indent >= INDENT) {
            newIndex += INDENT;
        } else if (state.isBlank()) {
            newIndex = state.getNextNonSpaceIndex();
        } else {
            return blockDidNotMatch();
        }
        return blockMatched(newIndex);
    }

    @Override
    public boolean acceptsLine() {
        return true;
    }

    @Override
    public void addLine(CharSequence line) {
        content.add(line);
    }

    @Override
    public void finalizeBlock(InlineParser inlineParser) {
        // add trailing newline
        content.add("");
        String contentString = content.getString();
        content = null;

        String literal = TRAILING_BLANK_LINES.matcher(contentString).replaceFirst("\n");
        block.setLiteral(literal);
    }

    @Override
    public Block getBlock() {
        return block;
    }

    public static class Factory extends AbstractBlockParserFactory {

        @Override
        public StartResult tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            int offset = state.getIndex();
            int nextNonSpace = state.getNextNonSpaceIndex();
            int indent = nextNonSpace - offset;
            boolean blank = nextNonSpace == state.getLine().length();
            // An indented code block cannot interrupt a paragraph.
            if (indent >= INDENT && !(state.getActiveBlockParser().getBlock() instanceof Paragraph) && !blank) {
                int newOffset = offset + INDENT;
                return start(new IndentedCodeBlockParser(pos(state, nextNonSpace)), newOffset, false);
            } else {
                return noStart();
            }
        }
    }
}

