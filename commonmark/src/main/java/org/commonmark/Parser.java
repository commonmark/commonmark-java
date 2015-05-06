package org.commonmark;

import org.commonmark.internal.BlockParserFactory;
import org.commonmark.internal.DocumentParser;
import org.commonmark.node.Node;

import java.util.ArrayList;
import java.util.List;

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
        private final List<BlockParserFactory> blockParserFactories = new ArrayList<>();

        public Parser build() {
            return new Parser(new DocumentParser(blockParserFactories));
        }

        public Builder customBlockParserFactory(BlockParserFactory blockParserFactory) {
            blockParserFactories.add(blockParserFactory);
            return this;
        }
    }
}
