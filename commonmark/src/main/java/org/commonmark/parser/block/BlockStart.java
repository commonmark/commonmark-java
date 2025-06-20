package org.commonmark.parser.block;

import org.commonmark.internal.BlockStartImpl;

/**
 * Result object for starting parsing of a block, see static methods for constructors.
 */
public abstract class BlockStart {

    protected BlockStart() {
    }

    /**
     * Result for when there is no block start.
     */
    public static BlockStart none() {
        return null;
    }

    /**
     * Start block(s) with the specified parser(s).
     */
    public static BlockStart of(BlockParser... blockParsers) {
        return new BlockStartImpl(blockParsers);
    }

    /**
     * Continue parsing at the specified index.
     *
     * @param newIndex the new index, see {@link ParserState#getIndex()}
     */
    public abstract BlockStart atIndex(int newIndex);

    /**
     * Continue parsing at the specified column (for tab handling).
     *
     * @param newColumn the new column, see {@link ParserState#getColumn()}
     */
    public abstract BlockStart atColumn(int newColumn);

    /**
     * @deprecated use {@link #replaceParagraphLines(int)} instead; please raise an issue if that doesn't work for you
     * for some reason.
     */
    @Deprecated
    public abstract BlockStart replaceActiveBlockParser();

    /**
     * Replace a number of lines from the current paragraph (as returned by
     * {@link MatchedBlockParser#getParagraphLines()}) with the new block.
     * <p>
     * This is useful for parsing blocks that start with normal paragraphs and only have special marker syntax in later
     * lines, e.g. in this:
     * <pre>
     * Foo
     * ===
     * </pre>
     * The <code>Foo</code> line is initially parsed as a normal paragraph, then <code>===</code> is parsed as a heading
     * marker, replacing the 1 paragraph line before. The end result is a single Heading block.
     * <p>
     * Note that source spans from the replaced lines are automatically added to the new block.
     *
     * @param lines the number of lines to replace (at least 1); use {@link Integer#MAX_VALUE} to replace the whole
     *              paragraph
     */
    public abstract BlockStart replaceParagraphLines(int lines);

}
