package org.commonmark.parser;

import org.commonmark.internal.inline.InlineContentParser;
import org.commonmark.internal.inline.InlineContentParserFactory;
import org.commonmark.internal.inline.InlineParserState;
import org.commonmark.internal.inline.ParsedInline;
import org.commonmark.node.CustomNode;
import org.commonmark.node.Heading;
import org.commonmark.test.Nodes;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;

public class InlineContentParserTest {

    @Test
    public void customInlineContentParser() {
        var parser = Parser.builder().customInlineContentParser(new DollarInlineParser.Factory()).build();
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
}
