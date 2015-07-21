package org.commonmark.parser;

import org.commonmark.Extension;
import org.commonmark.parser.block.BlockParserFactory;
import org.commonmark.internal.DocumentParser;
import org.commonmark.node.Node;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    private final List<BlockParserFactory> blockParserFactories;
    private final List<DelimiterProcessor> delimiterProcessors;
    private final List<PostProcessor> postProcessors;

    private Parser(Builder builder) {
        this.blockParserFactories = builder.blockParserFactories;
        this.delimiterProcessors = builder.delimiterProcessors;
        this.postProcessors = builder.postProcessors;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Parse the specified input text into a AST (tree of nodes).
     * <p>
     * Note that this method is thread-safe (a new parser state is used for each invocation).
     *
     * @param input the text to parse
     * @return the root node
     */
    public Node parse(String input) {
        DocumentParser documentParser = new DocumentParser(blockParserFactories, delimiterProcessors);
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
            return new Parser(this);
        }

        /**
         * @param extensions extensions to use on this parser
         * @return this
         */
        public Builder extensions(Iterable<? extends Extension> extensions) {
            for (Extension extension : extensions) {
                if (extension instanceof ParserExtension) {
                    ParserExtension parserExtension = (ParserExtension) extension;
                    parserExtension.extend(this);
                }
            }
            return this;
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

    /**
     * Extension for parser.
     */
    public interface ParserExtension extends Extension {
        void extend(Builder parserBuilder);
    }
}
