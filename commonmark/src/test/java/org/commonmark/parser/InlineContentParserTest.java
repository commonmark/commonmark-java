package org.commonmark.parser;

import org.commonmark.node.*;
import org.commonmark.parser.beta.InlineContentParser;
import org.commonmark.parser.beta.InlineContentParserFactory;
import org.commonmark.parser.beta.InlineParserState;
import org.commonmark.parser.beta.ParsedInline;
import org.commonmark.test.Nodes;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class InlineContentParserTest {

    @Test
    void customInlineContentParser() {
        var parser = Parser.builder().customInlineContentParserFactory(new DollarInlineParser.Factory()).build();
        var doc = parser.parse("Test: $hey *there*$ $you$\n\n# Heading $heading$\n");
        var inline1 = Nodes.find(doc, DollarInline.class);
        assertThat(inline1.getLiteral()).isEqualTo("hey *there*");

        var inline2 = (DollarInline) doc.getFirstChild().getLastChild();
        assertThat(inline2.getLiteral()).isEqualTo("you");

        var heading = Nodes.find(doc, Heading.class);
        var inline3 = (DollarInline) heading.getLastChild();
        assertThat(inline3.getLiteral()).isEqualTo("heading");

        // Parser is created for each inline snippet, which is why the index resets for the second snippet.
        assertThat(inline1.getIndex()).isEqualTo(0);
        assertThat(inline2.getIndex()).isEqualTo(1);
        assertThat(inline3.getIndex()).isEqualTo(0);
    }

    @Test
    void bangInlineContentParser() {
        // See if using ! for a custom inline content parser works.
        // ![] is used for images, but if it's not followed by a [, it should be possible to parse it differently.
        var parser = Parser.builder().customInlineContentParserFactory(new BangInlineParser.Factory()).build();
        var doc = parser.parse("![image](url) !notimage");
        var image = Nodes.find(doc, Image.class);
        assertThat(image.getDestination()).isEqualTo("url");
        assertThat(((Text) image.getNext()).getLiteral()).isEqualTo(" ");
        // Class
        assertThat(image.getNext().getNext()).isInstanceOf(BangInline.class);
        assertThat(((Text) image.getNext().getNext().getNext()).getLiteral()).isEqualTo("notimage");
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

    private static class BangInline extends CustomNode {
    }

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
