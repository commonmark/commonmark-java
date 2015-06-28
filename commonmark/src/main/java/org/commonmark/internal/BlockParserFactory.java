package org.commonmark.internal;

import org.commonmark.parser.BlockStart;

public interface BlockParserFactory {

    BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser);

}
