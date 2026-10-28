package org.commonmark.parser;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.commonmark.node.*;
import org.commonmark.parser.beta.InlineContentParser;
import org.commonmark.parser.beta.InlineContentParserFactory;
import org.commonmark.parser.beta.InlineParserState;
import org.commonmark.parser.beta.ParsedInline;
import org.commonmark.test.Nodes;
import org.commonmark.text.AsciiMatcher;
import org.commonmark.text.CharMatcher;
import org.junit.jupiter.api.Test;

class InlineContentParserTest {

    @Test
    void customInlineContentParser() {
        var parser =
                Parser.builder()
                        .customInlineContentParserFactory(new DollarInlineParser.Factory())
                        .build();
        var doc = parser.parse("Test: $hey *there*$ $you$\n\n# Heading $heading$\n");
        var inline1 = Nodes.find(doc, DollarInline.class);
        assertThat(inline1.getLiteral()).isEqualTo("hey *there*");

        var inline2 = (DollarInline) doc.getFirstChild().getLastChild();
        assertThat(inline2.getLiteral()).isEqualTo("you");

        var heading = Nodes.find(doc, Heading.class);
        var inline3 = (DollarInline) heading.getLastChild();
        assertThat(inline3.getLiteral()).isEqualTo("heading");

        // Parser is created for each inline snippet, which is why the index resets for the second
        // snippet.
        assertThat(inline1.getIndex()).isEqualTo(0);
        assertThat(inline2.getIndex()).isEqualTo(1);
        assertThat(inline3.getIndex()).isEqualTo(0);
    }

    @Test
    void bangInlineContentParser() {
        // See if using ! for a custom inline content parser works.
        // ![] is used for images, but if it's not followed by a [, it should be possible to parse
        // it differently.
        var parser =
                Parser.builder()
                        .customInlineContentParserFactory(new BangInlineParser.Factory())
                        .build();
        var doc = parser.parse("![image](url) !notimage");
        var image = Nodes.find(doc, Image.class);
        assertThat(image.getDestination()).isEqualTo("url");
        assertThat(((Text) image.getNext()).getLiteral()).isEqualTo(" ");
        // Class
        assertThat(image.getNext().getNext()).isInstanceOf(BangInline.class);
        assertThat(((Text) image.getNext().getNext().getNext()).getLiteral()).isEqualTo("notimage");
    }

    @Test
    void backwardSearchInlineContentParser() {
        var parser =
                Parser.builder()
                        .customInlineContentParserFactory(new DestinationInlineParser.Factory())
                        .build();

        var doc = parser.parse("img(a-link) more text");
        var inline = Nodes.find(doc, DestinationInline.class);
        assertThat(((Text) inline.getPrevious()).getLiteral()).isEqualTo("img");
        assertThat(inline.getDestination()).isEqualTo("a-link");
        assertThat(((Text) inline.getNext()).getLiteral()).isEqualTo(" more text");

        doc = parser.parse("img(a-link-)more-text) more text");
        inline = Nodes.find(doc, DestinationInline.class);
        assertThat(((Text) inline.getPrevious()).getLiteral()).isEqualTo("img");
        assertThat(inline.getDestination()).isEqualTo("a-link-)more-text");
        assertThat(((Text) inline.getNext()).getLiteral()).isEqualTo(" more text");

        doc = parser.parse("img(a-link-)more-text)");
        inline = Nodes.find(doc, DestinationInline.class);
        assertThat(inline.getDestination()).isEqualTo("a-link-)more-text");
        assertThat(inline.getNext()).isNull();

        doc = parser.parse("img() more text");
        inline = Nodes.find(doc, DestinationInline.class);
        assertThat(inline.getDestination()).isEqualTo("");
        assertThat(((Text) inline.getNext()).getLiteral()).isEqualTo(" more text");

        doc = parser.parse("img(not-a-link more text");
        assertThat(Nodes.tryFind(doc, DestinationInline.class)).isNull();
        assertThat(((Text) doc.getFirstChild().getFirstChild()).getLiteral())
                .isEqualTo("img(not-a-link more text");

        // The ')' before the saved opening position is outside the inclusive bound.
        doc = parser.parse("before)img(not-a-link more text");
        assertThat(Nodes.tryFind(doc, DestinationInline.class)).isNull();
        assertThat(((Text) doc.getFirstChild().getFirstChild()).getLiteral())
                .isEqualTo("before)img(not-a-link more text");
    }

    private static class DollarInline extends CustomNode {
        private final String literal;
        private final int index;

        public DollarInline(String literal, int index) {
            this.literal = literal;
            this.index = index;
        }

        public String getLiteral() {
            return literal;
        }

        public int getIndex() {
            return index;
        }
    }

    private static class DollarInlineParser implements InlineContentParser {

        private int index = 0;

        @Override
        public ParsedInline tryParse(InlineParserState inlineParserState) {
            var scanner = inlineParserState.scanner();
            scanner.next();
            var pos = scanner.position();

            var end = scanner.find('$');
            if (end == -1) {
                return ParsedInline.none();
            }
            var content = scanner.getSource(pos, scanner.position()).getContent();
            scanner.next();
            return ParsedInline.of(new DollarInline(content, index++), scanner.position());
        }

        static class Factory implements InlineContentParserFactory {
            @Override
            public Set<Character> getTriggerCharacters() {
                return Set.of('$');
            }

            @Override
            public InlineContentParser create() {
                return new DollarInlineParser();
            }
        }
    }

    /**
     * Custom image-like syntax {@code (destination)} whose destination may itself contain {@code
     * )}. The closer is the last {@code )} before whitespace or the end of input, and it must not
     * be before the opening parenthesis.
     */
    private static class DestinationInline extends CustomNode {
        private final String destination;

        public DestinationInline(String destination) {
            this.destination = destination;
        }

        public String getDestination() {
            return destination;
        }
    }

    private static class DestinationInlineParser implements InlineContentParser {

        private static final CharMatcher WHITESPACE =
                AsciiMatcher.builder().anyOf(" \t\n\u000B\f\r").build();

        @Override
        public ParsedInline tryParse(InlineParserState inlineParserState) {
            var scanner = inlineParserState.scanner();
            scanner.next();
            var contentStart = scanner.position();
            scanner.find(WHITESPACE);
            if (scanner.findPrev(')', contentStart) == -1) {
                return ParsedInline.none();
            }
            var destination = scanner.getSource(contentStart, scanner.position()).getContent();
            scanner.next();
            return ParsedInline.of(new DestinationInline(destination), scanner.position());
        }

        static class Factory implements InlineContentParserFactory {
            @Override
            public Set<Character> getTriggerCharacters() {
                return Set.of('(');
            }

            @Override
            public InlineContentParser create() {
                return new DestinationInlineParser();
            }
        }
    }

    private static class BangInline extends CustomNode {}

    private static class BangInlineParser implements InlineContentParser {

        @Override
        public ParsedInline tryParse(InlineParserState inlineParserState) {
            var scanner = inlineParserState.scanner();
            scanner.next();
            return ParsedInline.of(new BangInline(), scanner.position());
        }

        static class Factory implements InlineContentParserFactory {
            @Override
            public Set<Character> getTriggerCharacters() {
                return Set.of('!');
            }

            @Override
            public InlineContentParser create() {
                return new BangInlineParser();
            }
        }
    }
}
