package org.commonmark.parser;

import org.commonmark.Extension;
import org.commonmark.internal.DocumentParser;
import org.commonmark.internal.InlineParserImpl;
import org.commonmark.node.Node;
import org.commonmark.parser.block.BlockParserFactory;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

public class Parser {

    private final List<BlockParserFactory> blockParserFactories;
    private final Map<Character, DelimiterProcessor> delimiterProcessors;
    private final BitSet delimiterCharacters;
    private final BitSet specialCharacters;
    private final List<PostProcessor> postProcessors;

    private Parser(Builder builder) {
        this.blockParserFactories = DocumentParser.calculateBlockParserFactories(builder.blockParserFactories);
        this.delimiterProcessors = InlineParserImpl.calculateDelimiterProcessors(builder.delimiterProcessors);
        this.delimiterCharacters = InlineParserImpl.calculateDelimiterCharacters(delimiterProcessors.keySet());
        this.specialCharacters = InlineParserImpl.calculateSpecialCharacters(delimiterCharacters);
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
        InlineParserImpl inlineParser = new InlineParserImpl(specialCharacters, delimiterCharacters, delimiterProcessors);
        DocumentParser documentParser = new DocumentParser(blockParserFactories, inlineParser);
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
