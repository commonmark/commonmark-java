package org.commonmark.internal;

import org.commonmark.node.Block;
import org.commonmark.node.ListItem;
import org.commonmark.node.SourcePosition;
import org.commonmark.parser.block.AbstractBlockParser;
import org.commonmark.parser.block.BlockContinue;
import org.commonmark.parser.block.ParserState;

public class ListItemParser extends AbstractBlockParser {

    private final ListItem block = new ListItem();

    private int itemIndent;

    public ListItemParser(int itemIndent, SourcePosition pos) {
        this.itemIndent = itemIndent;
        block.setSourcePosition(pos);
    }

    @Override
    public boolean isContainer() {
        return true;
    }

    @Override
    public boolean canContain(Block block) {
        return true;
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public BlockContinue tryContinue(ParserState state) {
        if (state.isBlank()) {
            return BlockContinue.atIndex(state.getNextNonSpaceIndex());
        }

        if (state.getIndent() >= itemIndent) {
            return BlockContinue.atColumn(state.getColumn() + itemIndent);
        } else {
            return BlockContinue.none();
        }
    }

}
