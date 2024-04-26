package org.commonmark.internal.inline;

import org.commonmark.node.Node;
import org.commonmark.parser.beta.ParsedInline;
import org.commonmark.parser.beta.Position;

public class ParsedInlineImpl implements ParsedInline {
    private final Node node;
    private final Position position;

    public ParsedInlineImpl(Node node, Position position) {
        this.node = node;
        this.position = position;
    }

    public Node getNode() {
        return node;
    }

    public Position getPosition() {
        return position;
    }
}
