package org.commonmark;

import org.commonmark.internal.BlockParserFactory;
import org.commonmark.internal.DocumentParser;
import org.commonmark.node.Document;
import org.commonmark.node.Node;
import org.commonmark.parser.DelimiterProcessor;
import org.commonmark.parser.PostProcessor;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    private final DocumentParser documentParser;
    private final List<PostProcessor> postProcessors;

    private Parser(DocumentParser documentParser, List<PostProcessor> postProcessors) {
        this.documentParser = documentParser;
        this.postProcessors = postProcessors;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Node parse(String input) {
        Node document = documentParser.parse(input);
        for (PostProcessor postProcessor : postProcessors) {
            document = postProcessor.process(document);
        }
        return document;
    }

    public static class Builder {
        private final List<BlockParserFactory> blockParserFactories = new ArrayList<>();
        private final List<DelimiterProcessor> delimiterProcessors = new ArrayList<>();
        private final List<PostProcessor> postProcessors = new ArrayList<>();

        public Parser build() {
            return new Parser(new DocumentParser(blockParserFactories, delimiterProcessors), postProcessors);
        }

        public Builder customBlockParserFactory(BlockParserFactory blockParserFactory) {
            blockParserFactories.add(blockParserFactory);
            return this;
        }

        public Builder customDelimiterProcessor(DelimiterProcessor delimiterProcessor) {
            delimiterProcessors.add(delimiterProcessor);
            return this;
        }

        public Builder postProcessor(PostProcessor postProcessor) {
            postProcessors.add(postProcessor);
            return this;
        }
    }
}
