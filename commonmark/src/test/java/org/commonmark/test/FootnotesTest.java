package org.commonmark.test;

import org.commonmark.Extension;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.parser.block.*;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class FootnotesTest {

    public static class FootnoteDefinition extends CustomBlock {

        private String label;

        public FootnoteDefinition(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public static class FootnotesExtension implements Parser.ParserExtension {

        public static Extension create() {
            return new FootnotesExtension();
        }

        @Override
        public void extend(Parser.Builder parserBuilder) {
            parserBuilder.customBlockParserFactory(new FootnoteBlockParser.Factory());
        }
    }

    public static class FootnoteBlockParser extends AbstractBlockParser {

        private final FootnoteDefinition block;

        public FootnoteBlockParser(String label) {
            block = new FootnoteDefinition(label);
        }

        @Override
        public Block getBlock() {
            return block;
        }

        @Override
        public boolean canHaveLazyContinuationLines() {
            return true;
        }

        @Override
        public BlockContinue tryContinue(ParserState parserState) {
            // We're not continuing to give other block parsers a chance to interrupt this definition.
            // But if no other block parser applied (including another FootnotesBlockParser), we will
            // accept the line via lazy continuation.
            return BlockContinue.none();
        }

        public static class Factory implements BlockParserFactory {

            @Override
            public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
                var content = state.getLine().getContent();
                // TODO: Can it be indented? Maybe less than code block indent.
                var index = state.getNextNonSpaceIndex();
                if (content.charAt(index) != '[' || index + 1 >= content.length()) {
                    return BlockStart.none();
                }
                index++;
                if (content.charAt(index) != '^' || index + 1 >= content.length()) {
                    return BlockStart.none();
                }
                // Now at first label character (if any)
                index++;

                for (int i = index; i < content.length(); i++) {
                    var c = content.charAt(i);
                    if (c == ']') {
                        if (i > index) {
                            var label = content.subSequence(index, i).toString();
                            return BlockStart.of(new FootnoteBlockParser(label));
                        } else {
                            return BlockStart.none();
                        }
                    }
                    // TODO: Check what GitHub actually does here, e.g. tabs, control characters, other Unicode whitespace
                    if (Character.isWhitespace(c)) {
                        return BlockStart.none();
                    }
                }

                return BlockStart.none();
            }
        }
    }

    private final Parser PARSER = Parser.builder().extensions(List.of(FootnotesExtension.create())).build();

    @Test
    public void testBlockStart() {
        for (var s : List.of("1", "a")) {
            var doc = PARSER.parse("[^" + s + "]: footnote\n");
            var def = Nodes.find(doc, FootnoteDefinition.class);
            // TODO: Should label be "^1" instead?
            assertEquals(s, def.getLabel());
        }

        for (var s : List.of("", " ", "a b")) {
            var doc = PARSER.parse("[^" + s + "]: footnote\n");
            assertNull(Nodes.tryFind(doc, FootnoteDefinition.class));
        }

        // TODO: Test what characters are allowed for the label, e.g.
        //  [^], [^ ], [^^], [^[], [^*], [^\], [^\a], [^🙂], tab?, [^&], [^&amp;]
    }

    @Test
    public void testBlockStartInterrupts() {
        var doc = PARSER.parse("test\n[^1]: footnote\n");
        var paragraph = Nodes.find(doc, Paragraph.class);
        var def = Nodes.find(doc, FootnoteDefinition.class);
        assertEquals("test", ((Text) paragraph.getLastChild()).getLiteral());
        assertEquals("1", def.getLabel());
    }

    @Test
    public void testMultiple() {
        var doc = PARSER.parse("[^1]: foo\n[^2]: bar\n");
        var def1 = (FootnoteDefinition) doc.getFirstChild();
        var def2 = (FootnoteDefinition) doc.getLastChild();
        assertEquals("1", def1.getLabel());
        assertEquals("2", def2.getLabel());
    }

    @Test
    public void testBlockStartAfterLinkReferenceDefinition() {
        var doc = PARSER.parse("[foo]: /url\n[^1]: footnote\n");
        var linkReferenceDef = Nodes.find(doc, LinkReferenceDefinition.class);
        var footnotesDef = Nodes.find(doc, FootnoteDefinition.class);
        assertEquals("foo", linkReferenceDef.getLabel());
        assertEquals("1", footnotesDef.getLabel());
    }

    @Test
    public void testBlockContinue() {
        var doc = PARSER.parse("[^1]: footnote\nstill\n");
        var def = Nodes.find(doc, FootnoteDefinition.class);
        assertEquals("1", def.getLabel());
        assertNull(Nodes.tryFind(doc, Paragraph.class));
    }

    @Test
    public void testFootnotesDefinitionInterruptedByOthers() {
        var doc = PARSER.parse("[^1]: footnote\n# Heading\n");
        var def = Nodes.find(doc, FootnoteDefinition.class);
        var heading = Nodes.find(doc, Heading.class);
        assertEquals("1", def.getLabel());
        assertEquals("Heading", ((Text) heading.getFirstChild()).getLiteral());
    }
}
