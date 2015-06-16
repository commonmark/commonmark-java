package org.commonmark.internal;

public interface BlockParserFactory {

    StartResult tryStart(ParserState state);

    interface ParserState {
        CharSequence getLine();

        int getOffset();

        int getNextNonSpace();

        boolean isIndented();

        BlockParser getActiveBlockParser();

        BlockParser getMatchedBlockParser();

        /**
         * @return the first line of the paragraph if the active block is a paragraph and we're on the second line,
         * null otherwise
         */
        CharSequence getParagraphStartLine();

        int getLineNumber();

    }

    interface StartResult {
    }

    interface NoStart extends StartResult {
    }

    interface BlockStart extends StartResult {
        Iterable<BlockParser> getBlockParsers();

        int getNewOffset();

        boolean replaceActiveBlockParser();
    }

}
