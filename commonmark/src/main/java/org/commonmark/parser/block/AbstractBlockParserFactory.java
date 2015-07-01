package org.commonmark.parser.block;

import org.commonmark.node.SourcePosition;

public abstract class AbstractBlockParserFactory implements BlockParserFactory {

    protected SourcePosition pos(ParserState state, int columnNumber) {
        return new SourcePosition(state.getLineNumber(), columnNumber);
    }

}
