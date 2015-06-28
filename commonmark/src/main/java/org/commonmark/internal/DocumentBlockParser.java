package org.commonmark.internal;

import org.commonmark.node.Block;
import org.commonmark.node.Document;
import org.commonmark.parser.BlockContinue;

public class DocumentBlockParser extends AbstractBlockParser {

    private final Document document = new Document();

    @Override
    public BlockContinue tryContinue(ParserState state) {
        return BlockContinue.of(state.getIndex());
    }

    @Override
    public void addLine(CharSequence line) {
    }

    @Override
    public boolean canContain(Block block) {
        return true;
    }

    @Override
    public Document getBlock() {
        return document;
    }
}
