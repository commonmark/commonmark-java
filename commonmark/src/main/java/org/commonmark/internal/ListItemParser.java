package org.commonmark.internal;

import org.commonmark.node.Block;
import org.commonmark.node.ListBlock;
import org.commonmark.node.ListItem;
import org.commonmark.parser.block.AbstractBlockParser;
import org.commonmark.parser.block.BlockContinue;
import org.commonmark.parser.block.ParserState;

public class ListItemParser extends AbstractBlockParser {

    private final ListItem block = new ListItem();

    /**
     * Minimum number of columns that the content has to be indented (relative to the containing
     * block) to be part of this list item.
     */
    private int contentIndent;

    private boolean lineBlank;

    private boolean previousLineBlank;

    public ListItemParser(int markerIndent, int contentIndent) {
        this.contentIndent = contentIndent;
        block.setMarkerIndent(markerIndent);
        block.setContentIndent(contentIndent);
    }

    @Override
    public boolean isContainer() {
        return true;
    }

    @Override
    public boolean canContain(Block childBlock) {
        if (previousLineBlank) {
            // A new block starts after a blank line, and blank lines after an empty list item end
            // the item, so this item already has a block before the blank line. That means the
            // list block is loose.
            //
            // spec: if any of its constituent list items directly contain two block-level elements
            // with a blank line between them
            Block parent = block.getParent();
            if (parent instanceof ListBlock) {
                ((ListBlock) parent).setTight(false);
            }
        }
        return true;
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public BlockContinue tryContinue(ParserState state) {
        // Only remember whether the previous line was blank, so that blank lines inside a block
        // that continues afterwards (e.g. a code block) don't affect if the list is tight.
        previousLineBlank = lineBlank;
        lineBlank = state.isBlank();
        if (lineBlank) {
            if (block.getFirstChild() == null) {
                // Blank line after empty list item
                return BlockContinue.none();
            } else {
                return BlockContinue.atIndex(state.getNextNonSpaceIndex());
            }
        }

        if (state.getIndent() >= contentIndent) {
            return BlockContinue.atColumn(state.getColumn() + contentIndent);
        } else {
            // Note: We'll hit this case for lazy continuation lines, they will get added later.
            return BlockContinue.none();
        }
    }
}
