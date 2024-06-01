package org.commonmark.internal.inline;

import org.commonmark.node.Node;
import org.commonmark.parser.beta.BracketResult;
import org.commonmark.parser.beta.Position;

public class BracketResultImpl implements BracketResult {
    @Override
    public BracketResult startFromBracket() {
        startFromBracket = true;
        return this;
    }

    public enum Type {
        WRAP,
        REPLACE
    }

    private final Type type;
    private final Node node;
    private final Position position;

    private boolean startFromBracket;

    public BracketResultImpl(Type type, Node node, Position position) {
        this.type = type;
        this.node = node;
        this.position = position;
    }

    public Type getType() {
        return type;
    }

    public Node getNode() {
        return node;
    }

    public Position getPosition() {
        return position;
    }

    public boolean isStartFromBracket() {
        return startFromBracket;
    }
}
