package org.commonmark.internal;

import org.commonmark.node.Block;
import org.commonmark.node.Document;

public class DocumentBlockParser extends AbstractBlockParser {

    private final Document document = new Document();

    @Override
    public ContinueResult tryContinue(ParserState state) {
        return blockMatched(state.getIndex());
    }

    @Override
    public void addLine(CharSequence line) {
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
    public Document getBlock() {
        return document;
    }
}
