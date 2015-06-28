package org.commonmark.parser;

import org.commonmark.internal.BlockParser;
import org.commonmark.internal.BlockStartImpl;

import java.util.Collections;

/**
 * Result object for starting parsing of a block, see static methods for constructors.
 */
public class BlockStart {

    protected BlockStart() {
    }

    public static BlockStart none() {
        return null;
    }

    public static BlockStart of(BlockParser blockParser, int newIndex) {
        return new BlockStartImpl(Collections.singleton(blockParser), newIndex, false);
    }

    public static BlockStart of(BlockParser blockParser, int newIndex, boolean replaceActiveBlockParser) {
        return new BlockStartImpl(Collections.singleton(blockParser), newIndex, replaceActiveBlockParser);
    }

    public static BlockStart of(Iterable<BlockParser> blockParsers, int newIndex, boolean replaceActiveBlockParser) {
        return new BlockStartImpl(blockParsers, newIndex, replaceActiveBlockParser);
    }

}
