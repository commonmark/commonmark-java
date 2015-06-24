package org.commonmark.internal;

import org.commonmark.internal.util.Parsing;
import org.commonmark.node.Block;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SourcePosition;

public class ParagraphParser extends AbstractBlockParser {

    private final Paragraph block = new Paragraph();
    // TODO: Can this be inlined?
    private BlockContent content = new BlockContent();

    public ParagraphParser(SourcePosition pos) {
        block.setSourcePosition(pos);
    }

    @Override
    public ContinueResult tryContinue(ParserState state) {
        if (!state.isBlank()) {
            return blockMatched(state.getIndex());
        } else {
            return blockDidNotMatch();
        }
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
        String contentString = content.getString();
        boolean hasReferenceDefs = false;

        int pos;
        // try parsing the beginning as link reference definitions:
        while (contentString.length() > 3 && contentString.charAt(0) == '[' &&
                (pos = inlineParser.parseReference(contentString)) != 0) {
            contentString = contentString.substring(pos);
            hasReferenceDefs = true;
        }
        if (hasReferenceDefs && Parsing.isBlank(contentString)) {
            block.unlink();
            content = null;
        } else {
            content = new BlockContent(contentString);
        }
    }

    @Override
    public void processInlines(InlineParser inlineParser) {
        if (content != null) {
            inlineParser.parse(block, content.getString());
        }
    }

    @Override
    public boolean canContain(Block block) {
        return false;
    }

    @Override
    public boolean shouldTryBlockStarts() {
        return true;
    }

    @Override
    public Block getBlock() {
        return block;
    }

    public boolean hasSingleLine() {
        return content.hasSingleLine();
    }

    public String getContentString() {
        return content.getString();
    }
}
