package org.commonmark.internal;

import org.commonmark.parser.block.BlockContinue;

public class BlockContinueImpl extends BlockContinue {

    private final int newIndex;
    private final boolean finalize;

    public BlockContinueImpl(int newIndex, boolean finalize) {
        this.newIndex = newIndex;
        this.finalize = finalize;
    }

    public int getNewIndex() {
        return newIndex;
    }

    public boolean isFinalize() {
        return finalize;
    }

}
