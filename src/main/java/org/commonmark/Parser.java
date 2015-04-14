package org.commonmark;

import org.commonmark.internal.DocumentParser;
import org.commonmark.node.Node;

public class Parser {

    private final DocumentParser documentParser;

    private Parser(DocumentParser documentParser) {
        this.documentParser = documentParser;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Node parse(String input) {
        return documentParser.parse(input);
    }

    public static class Builder {
        public Parser build() {
            return new Parser(new DocumentParser());
        }
    }
}
