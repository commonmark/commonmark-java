package org.commonmark.parser;

import org.commonmark.node.CustomNode;
import org.commonmark.node.Heading;
import org.commonmark.node.Image;
import org.commonmark.node.Text;
import org.commonmark.parser.beta.InlineContentParser;
import org.commonmark.parser.beta.InlineContentParserFactory;
import org.commonmark.parser.beta.InlineParserState;
import org.commonmark.parser.beta.ParsedInline;
import org.commonmark.test.Nodes;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;

public class InlineContentParserTest {

    @Test
    public void customInlineContentParser() {
        var parser = Parser.builder().customInlineContentParserFactory(new DollarInlineParser.Factory()).build();
        var doc = parser.parse("Test: $hey *there*$ $you$\n\n# Heading $heading$\n");
        var inline1 = Nodes.find(doc, DollarInline.class);
        assertEquals("hey *there*", inline1.getLiteral());

        var inline2 = (DollarInline) doc.getFirstChild().getLastChild();
        assertEquals("you", inline2.getLiteral());

        var heading = Nodes.find(doc, Heading.class);
        var inline3 = (DollarInline) heading.getLastChild();
        assertEquals("heading", inline3.getLiteral());

        // Parser is created for each inline snippet, which is why the index resets for the second snippet.
        assertEquals(0, inline1.getIndex());
        assertEquals(1, inline2.getIndex());
        assertEquals(0, inline3.getIndex());
    }

    @Test
    public void bangInlineContentParser() {
        // See if using ! for a custom inline content parser works.
        // ![] is used for images, but if it's not followed by a [, it should be possible to parse it differently.
        var parser = Parser.builder().customInlineContentParserFactory(new BangInlineParser.Factory()).build();
        var doc = parser.parse("![image](url) !notimage");
        var image = Nodes.find(doc, Image.class);
        assertEquals("url", image.getDestination());
        assertEquals(" ", ((Text) image.getNext()).getLiteral());
        assertEquals(BangInline.class, image.getNext().getNext().getClass());
        assertEquals("notimage", ((Text) image.getNext().getNext().getNext()).getLiteral());
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
