package org.commonmark.internal;

import org.commonmark.node.Block;

public abstract class AbstractBlockParser implements BlockParser {

    @Override
    public boolean canContain(Block block) {
        return false;
    }

    @Override
    public boolean shouldTryBlockStarts() {
        return false;
    }

    @Override
    public boolean acceptsLine() {
        return false;
    }

    @Override
    public void addLine(CharSequence line) {
    }

    @Override
    public void finalizeBlock(InlineParser inlineParser) {
    }

    @Override
    public void processInlines(InlineParser inlineParser) {
    }

}
