package org.commonmark.internal.inline;

import org.commonmark.node.Node;

public abstract class ParsedInline {

    protected ParsedInline() {
    }

    public static ParsedInline none() {
        return null;
    }

    public static ParsedInline of(Node node, int consumed) {
        if (node == null) {
            throw new NullPointerException("node must not be null");
        }
        if (consumed <= 0) {
            throw new IllegalArgumentException("consumed must be greater than 0");
        }
        return new ParsedInlineImpl(node, consumed);
    }
}
