package org.commonmark.internal;

import org.commonmark.node.Block;
import org.commonmark.node.ListItem;
import org.commonmark.node.SourcePosition;

public class ListItemParser extends AbstractBlockParser {

    private final ListItem block = new ListItem();

    private int itemOffset;

    public ListItemParser(int itemOffset, SourcePosition pos) {
        this.itemOffset = itemOffset;
        block.setSourcePosition(pos);
    }

    @Override
    public ContinueResult tryContinue(ParserState state) {
        if (state.isBlank()) {
            return blockMatched(state.getNextNonSpaceIndex());
        }

        int indent = state.getNextNonSpaceIndex() - state.getIndex();
        if (indent >= itemOffset) {
            int newIndex = state.getIndex() + itemOffset;
            return blockMatched(newIndex);
        } else {
            return blockDidNotMatch();
        }
    }

    @Override
    public boolean canContain(Block block) {
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