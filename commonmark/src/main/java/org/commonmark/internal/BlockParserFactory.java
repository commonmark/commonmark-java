package org.commonmark.internal;

public interface BlockParserFactory {

    StartResult tryStart(ParserState state);

    interface ParserState {
        CharSequence getLine();

        int getOffset();

        int getNextNonSpace();

        BlockParser getActiveBlockParser();

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
