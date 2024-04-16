package org.commonmark.parser;

import org.commonmark.internal.inline.InlineContentParser;
import org.commonmark.internal.inline.InlineParserState;
import org.commonmark.internal.inline.ParsedInline;
import org.commonmark.node.CustomNode;
import org.commonmark.test.Nodes;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CustomInlineContentParserTest {

    @Test
    public void customInlineContentParser() {
        var parser = Parser.builder().customInlineContentParser(new DollarInlineContentParser()).build();
        var doc = parser.parse("Test: $hey *there*$");
        var dollarInline = Nodes.find(doc, DollarInline.class);
        assertEquals("hey *there*", dollarInline.getLiteral());
    }

    private static class DollarInline extends CustomNode {
        private final String literal;

        public DollarInline(String literal) {
            this.literal = literal;
        }

        public String getLiteral() {
            return literal;
        }
    }

    private static class DollarInlineContentParser implements InlineContentParser {
        @Override
        public char getTriggerCharacter() {
            return '$';
        }

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
            return ParsedInline.of(new DollarInline(content), scanner.position());
        }
    }
}
