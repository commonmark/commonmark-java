package org.commonmark.parser;

import java.io.IOException;
import java.io.Reader;
import org.commonmark.Extension;
import org.commonmark.internal.DocumentParser;
import org.commonmark.internal.InlineParserImpl;
import org.commonmark.node.Block;
import org.commonmark.node.Node;
import org.commonmark.parser.block.BlockParserFactory;
import org.commonmark.parser.delimiter.DelimiterProcessor;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

/**
 * Parses input text to a tree of nodes.
 * <p>
 * Start with the {@link #builder} method, configure the parser and build it. Example:
 * <pre><code>
 * Parser parser = Parser.builder().build();
 * Node document = parser.parse("input text");
 * </code></pre>
 */
public class Parser {

    private final List<BlockParserFactory> blockParserFactories;
    private final Map<Character, DelimiterProcessor> delimiterProcessors;
    private final BitSet delimiterCharacters;
    private final BitSet specialCharacters;
    private final List<PostProcessor> postProcessors;

    private Parser(Builder builder) {
        this.blockParserFactories = DocumentParser.calculateBlockParserFactories(builder.blockParserFactories, builder.allowedBlockTypes);
        this.delimiterProcessors = InlineParserImpl.calculateDelimiterProcessors(builder.delimiterProcessors);
        this.delimiterCharacters = InlineParserImpl.calculateDelimiterCharacters(delimiterProcessors.keySet());
        this.specialCharacters = InlineParserImpl.calculateSpecialCharacters(delimiterCharacters);
        this.postProcessors = builder.postProcessors;
    }

    /**
     * Create a new builder for configuring a {@link Parser}.
     *
     * @return a builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Parse the specified input text into a tree of nodes.
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
        return postProcess(document);
    }

    /**
     * Parse the specified reader into a tree of nodes. The caller is responsible for closing the reader.
     * <p>
     * Note that this method is thread-safe (a new parser state is used for each invocation).
     *
     * @param input the reader to parse
     * @return the root node
     * @throws IOException when reading throws an exception
     */
    public Node parseReader(Reader input) throws IOException {
        InlineParserImpl inlineParser = new InlineParserImpl(specialCharacters, delimiterCharacters, delimiterProcessors);
        DocumentParser documentParser = new DocumentParser(blockParserFactories, inlineParser);
        Node document = documentParser.parse(input);
        return postProcess(document);
    }

    private Node postProcess(Node document) {
        for (PostProcessor postProcessor : postProcessors) {
            document = postProcessor.process(document);
        }
        return document;
    }

    /**
     * Builder for configuring a {@link Parser}.
     */
    public static class Builder {
        private final List<BlockParserFactory> blockParserFactories = new ArrayList<>();
        private final List<DelimiterProcessor> delimiterProcessors = new ArrayList<>();
        private final List<PostProcessor> postProcessors = new ArrayList<>();
        private List<Class<? extends Block>> allowedBlockTypes = DocumentParser.getDefaultBlockParserTypes();

        /**
         * @return the configured {@link Parser}
         */
        public Parser build() {
            return new Parser(this);
        }

        /**
         * @param extensions extensions to use on this parser
         * @return {@code this}
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

        /**
         * Describe the list of markdown features the parser will recognize and parse.
         *
         * By default, Commonmark will recognize and parse the following set of core markdown features:
         *
         * Heading (#) - Heading.class
         * HTML (<html></html>) - HtmlBlock.class
         * Horizontal Rule / Thematic Break (---) - ThematicBreak.class
         * Fenced Code Block (```) - FencedCodeBlock.class
         * Indented Code Block - IndentedCodeBlock.class
         * Block Quote (>) - BlockQuote.class
         * Ordered / Unordered List (>) - ListBlock.class
         *
         * To parse only a subset of the features listed above, pass a list of each feature's associated Node class.
         * E.g., to only parse Headings and Lists:
         * <pre>
         *     {@code
         *     Parser.builder().allowedBlockTypes(Heading.class, ListBlock.class);
         *     }
         * </pre>
         *
         * @param allowedBlockTypes A list of nodes the parser will parse.
         *                     If this list is empty, the parser will not recognize any Commonmark core markdown features.
         *
         * @return {@code this}
         */
        public Builder allowedBlockTypes(List<Class<? extends Block>> allowedBlockTypes) {
            this.allowedBlockTypes = allowedBlockTypes;
            return this;
        }

        /**
         * Adds a custom block parser factory.
         * <p>
         * Note that custom factories are applied <em>before</em> the built-in factories. This is so that
         * extensions can change how some syntax is parsed that would otherwise be handled by built-in factories.
         * "With great power comes great responsibility."
         *
         * @param blockParserFactory a block parser factory implementation
         * @return {@code this}
         */
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
     * Extension for {@link Parser}.
     */
    public interface ParserExtension extends Extension {
        void extend(Builder parserBuilder);
    }
}
