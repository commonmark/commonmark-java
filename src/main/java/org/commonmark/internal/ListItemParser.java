package org.commonmark.internal;

import org.commonmark.node.Block;
import org.commonmark.node.ListItem;
import org.commonmark.node.Node;
import org.commonmark.node.SourcePosition;

public class ListItemParser extends AbstractBlockParser {

    private final ListItem block = new ListItem();

    private int itemOffset;

    public ListItemParser(int itemOffset, SourcePosition pos) {
        this.itemOffset = itemOffset;
        block.setSourcePosition(pos);
    }

    @Override
    public ContinueResult continueBlock(String line, int nextNonSpace, int offset, boolean blank) {
        int indent = nextNonSpace - offset;
        if (blank) {
            return blockMatched(nextNonSpace);
        } else if (indent >= itemOffset) {
            int newOffset = offset + itemOffset;
            return blockMatched(newOffset);
        } else {
            return blockDidNotMatch();
        }
    }

    @Override
    public boolean canContain(Node.Type type) {
        return true;
    }

    @Override
    public boolean shouldTryBlockStarts() {
        return true;
    }

    @Override
    public Block getBlock() {
        return block;
    }

}