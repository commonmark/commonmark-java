package org.commonmark.parser.beta;

import org.commonmark.internal.inline.BracketResultImpl;
import org.commonmark.node.Node;

public interface BracketResult {
    static BracketResult none() {
        return null;
    }

    static BracketResult wrapTextIn(Node node, Position position) {
        return new BracketResultImpl(BracketResultImpl.Type.WRAP, node, position);
    }

    static BracketResult replaceWith(Node node, Position position) {
        return new BracketResultImpl(BracketResultImpl.Type.REPLACE, node, position);
    }

    BracketResult startFromBracket();
}
