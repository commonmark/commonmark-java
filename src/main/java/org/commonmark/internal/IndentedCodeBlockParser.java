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
    public ContinueResult continueBlock(String line, int nextNonSpace, int offset, boolean blank) {
        int indent = nextNonSpace - offset;
        int newOffset = offset;
        if (indent >= INDENT) {
            newOffset += INDENT;
        } else if (blank) {
            newOffset = nextNonSpace;
        } else {
            return blockDidNotMatch();
        }
        return blockMatched(newOffset);
    }

    @Override
    public boolean acceptsLine() {
        return true;
    }

    @Override
    public void addLine(String line) {
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
        public StartResult tryStart(ParserState state) {
            int offset = state.getOffset();
            int nextNonSpace = state.getNextNonSpace();
            int indent = nextNonSpace - offset;
            if (indent >= INDENT) {
                int newOffset = offset + INDENT;
                return start(new IndentedCodeBlockParser(pos(state, nextNonSpace)), newOffset, false);
            } else {
                return noStart();
            }
        }
    }
}

