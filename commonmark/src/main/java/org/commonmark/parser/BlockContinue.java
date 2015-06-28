package org.commonmark.parser;

import org.commonmark.internal.BlockContinueImpl;

/**
 * Result object for continuing parsing of a block, see static methods for constructors.
 */
public class BlockContinue {

    protected BlockContinue() {
    }

    public static BlockContinue none() {
        return null;
    }

    public static BlockContinue of(int newIndex) {
        return new BlockContinueImpl(newIndex, false);
    }

    public static BlockContinue finished() {
        return new BlockContinueImpl(0, true);
    }

}
