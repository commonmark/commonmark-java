package org.commonmark.internal;

public interface BlockParserFactory {

    StartResult tryStart(ParserState state, MatchedBlockParser matchedBlockParser);

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
