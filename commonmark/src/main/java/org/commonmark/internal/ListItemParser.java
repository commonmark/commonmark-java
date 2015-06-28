package org.commonmark.internal;

import org.commonmark.node.Block;
import org.commonmark.node.ListItem;
import org.commonmark.node.SourcePosition;
import org.commonmark.parser.BlockContinue;

public class ListItemParser extends AbstractBlockParser {

    private final ListItem block = new ListItem();

    private int itemOffset;

    public ListItemParser(int itemOffset, SourcePosition pos) {
        this.itemOffset = itemOffset;
        block.setSourcePosition(pos);
    }

    @Override
    public BlockContinue tryContinue(ParserState state) {
        if (state.isBlank()) {
            return BlockContinue.of(state.getNextNonSpaceIndex());
        }

        int indent = state.getNextNonSpaceIndex() - state.getIndex();
        if (indent >= itemOffset) {
            int newIndex = state.getIndex() + itemOffset;
            return BlockContinue.of(newIndex);
        } else {
            return BlockContinue.none();
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