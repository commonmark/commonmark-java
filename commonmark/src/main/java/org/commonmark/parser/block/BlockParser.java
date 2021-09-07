package org.commonmark.parser.block;

import org.commonmark.node.Block;
import org.commonmark.node.SourceSpan;
import org.commonmark.parser.InlineParser;
import org.commonmark.parser.SourceLine;

/**
 * Parser for a specific block node.
 * <p>
 * Implementations should subclass {@link AbstractBlockParser} instead of implementing this directly.
 */
public interface BlockParser {

    /**
     * @return true if the block that is parsed is a container (contains other blocks), or false if it's a leaf.
     */
    boolean isContainer();

    /**
     * <p>
     * Lazy continuation lines are lines that were rejected by this {@link #tryContinue(ParserState)} but didn't match
     * any other block parsers either.
     * <p>
     * If true is returned here, those lines will get added via {@link #addLine(SourceLine)}. For false, the block is
     * closed instead.
     * @return true if the block can have lazy continuation lines.
     */
    boolean canHaveLazyContinuationLines();

    boolean canContain(Block childBlock);

    Block getBlock();

    BlockContinue tryContinue(ParserState parserState);

    void addLine(SourceLine line);

    /**
     * Add a source span of the currently parsed block. The default implementation in {@link AbstractBlockParser} adds
     * it to the block. Unless you have some complicated parsing where you need to check source positions, you don't
     * need to override this.
     *
     * @since 0.16.0
     * @param sourceSpan Source span to be added into the current block
     */
    void addSourceSpan(SourceSpan sourceSpan);

    void closeBlock();

    void parseInlines(InlineParser inlineParser);

}
