package org.commonmark.internal;

import org.commonmark.internal.util.Parsing;
import org.commonmark.node.Block;
import org.commonmark.node.Node;
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
    public ContinueResult continueBlock(String line, int nextNonSpace, int offset, boolean blank) {
        if (!blank) {
            return blockMatched(offset);
        } else {
            return blockDidNotMatch();
        }
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
        int pos;
        String contentString = content.getString();

        // try parsing the beginning as link reference definitions:
        while (contentString.charAt(0) == '[' &&
                (pos = inlineParser.parseReference(contentString)) != 0) {
            contentString = contentString.substring(pos);
            if (Parsing.isBlank(contentString)) {
                block.unlink();
                break;
            }
        }
        content = new BlockContent(contentString);
    }

    @Override
    public void processInlines(InlineParser inlineParser) {
        inlineParser.parse(block, content.getString());
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

    public boolean hasLines() {
        return content.hasLines();
    }

    public String getContentString() {
        return content.getString();
    }
}
