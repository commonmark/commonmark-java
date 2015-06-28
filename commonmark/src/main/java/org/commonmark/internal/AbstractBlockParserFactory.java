package org.commonmark.internal;

import org.commonmark.node.SourcePosition;

import java.util.Collections;

public abstract class AbstractBlockParserFactory implements BlockParserFactory {

    protected SourcePosition pos(ParserState state, int columnNumber) {
        return new SourcePosition(state.getLineNumber(), columnNumber);
    }

}
