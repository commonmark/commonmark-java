package org.commonmark.parser.block;

import org.commonmark.node.Block;
import org.commonmark.node.SourceSpan;
import org.commonmark.parser.InlineParser;
import org.commonmark.parser.SourceLine;

public abstract class AbstractBlockParser implements BlockParser {

    @Override
    public boolean isContainer() {
        return false;
    }

    @Override
    public boolean canHaveLazyContinuationLines() {
        return false;
    }

    @Override
    public boolean canContain(Block childBlock) {
        return false;
    }

    @Override
    public void addLine(SourceLine line) {
    }

    @Override
    public void addSourceSpan(SourceSpan sourceSpan) {
        getBlock().addSourceSpan(sourceSpan);
    }

    @Override
    public void closeBlock() {
    }

    @Override
    public void parseInlines(InlineParser inlineParser) {
    }

}
