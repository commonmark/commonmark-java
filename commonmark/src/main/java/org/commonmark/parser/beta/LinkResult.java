package org.commonmark.parser.beta;

import org.commonmark.internal.inline.LinkResultImpl;
import org.commonmark.node.Node;

public interface LinkResult {
    static LinkResult none() {
        return null;
    }

    static LinkResult wrapTextIn(Node node, Position position) {
        return new LinkResultImpl(LinkResultImpl.Type.WRAP, node, position);
    }

    static LinkResult replaceWith(Node node, Position position) {
        return new LinkResultImpl(LinkResultImpl.Type.REPLACE, node, position);
    }

    LinkResult startFromBracket();
}
