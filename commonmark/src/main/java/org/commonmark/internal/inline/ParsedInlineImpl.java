package org.commonmark.internal.inline;

import org.commonmark.node.Node;

public class ParsedInlineImpl extends ParsedInline {
    private final Node node;
    private final int consumed;

    public ParsedInlineImpl(Node node, int consumed) {
        this.node = node;
        this.consumed = consumed;
    }

    public Node getNode() {
        return node;
    }

    public int getConsumed() {
        return consumed;
    }
}
