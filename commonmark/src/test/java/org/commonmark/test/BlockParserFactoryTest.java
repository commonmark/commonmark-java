package org.commonmark.test;

import org.commonmark.node.*;
import org.commonmark.parser.IncludeSourceSpans;
import org.commonmark.parser.InlineParser;
import org.commonmark.parser.Parser;
import org.commonmark.parser.SourceLines;
import org.commonmark.parser.block.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BlockParserFactoryTest {

    @Test
    public void customBlockParserFactory() {
        var parser = Parser.builder().customBlockParserFactory(new DashBlockParser.Factory()).build();

        // The dashes would normally be a ThematicBreak
        var doc = parser.parse("hey\n\n---\n");

        assertThat(doc.getFirstChild()).isInstanceOf(Paragraph.class);
        assertThat(((Text) doc.getFirstChild().getFirstChild()).getLiteral()).isEqualTo("hey");
        assertThat(doc.getLastChild()).isInstanceOf(DashBlock.class);
    }

    @Test
    public void replaceActiveBlockParser() {
        var parser = Parser.builder()
                .customBlockParserFactory(new StarHeadingBlockParser.Factory())
                .includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES)
                .build();

        var doc = parser.parse("a\nbc\n***\n");

        var heading = doc.getFirstChild();
        assertThat(heading).isInstanceOf(StarHeading.class);
        assertThat(heading.getNext()).isNull();
        var a = heading.getFirstChild();
        assertThat(a).isInstanceOf(Text.class);
        assertThat(((Text) a).getLiteral()).isEqualTo("a");
        var bc = a.getNext().getNext();
        assertThat(bc).isInstanceOf(Text.class);
        assertThat(((Text) bc).getLiteral()).isEqualTo("bc");
        assertThat(bc.getNext()).isNull();

        assertThat(heading.getSourceSpans()).isEqualTo(List.of(
                SourceSpan.of(0, 0, 0, 1),
                SourceSpan.of(1, 0, 2, 2),
                SourceSpan.of(2, 0, 5, 3)));
        assertThat(a.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(0, 0, 0, 1)));
        assertThat(bc.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(1, 0, 2, 2)));
    }

    private static class DashBlock extends CustomBlock {
    }

    private static class DashBlockParser extends AbstractBlockParser {

        private DashBlock dash = new DashBlock();

        @Override
        public Block getBlock() {
            return dash;
        }

        @Override
        public BlockContinue tryContinue(ParserState parserState) {
            return BlockContinue.none();
        }

        static class Factory extends AbstractBlockParserFactory {

            @Override
            public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
                if (state.getLine().getContent().equals("---")) {
                    return BlockStart.of(new DashBlockParser());
                }
                return BlockStart.none();
            }
        }
    }

    private static class StarHeading extends CustomBlock {
    }

    private static class StarHeadingBlockParser extends AbstractBlockParser {

        private final SourceLines content;
        private final StarHeading heading = new StarHeading();

        StarHeadingBlockParser(SourceLines content) {
            this.content = content;
        }

        @Override
        public Block getBlock() {
            return heading;
        }

        @Override
        public BlockContinue tryContinue(ParserState parserState) {
            return BlockContinue.none();
        }

        @Override
        public void parseInlines(InlineParser inlineParser) {
            inlineParser.parse(content, heading);
        }

        static class Factory extends AbstractBlockParserFactory {

            @Override
            public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
                var lines = matchedBlockParser.getParagraphLines();
                if (state.getLine().getContent().toString().startsWith("***")) {
                    return BlockStart.of(new StarHeadingBlockParser(lines))
                            .replaceActiveBlockParser();
                } else {
                    return BlockStart.none();
                }
            }
        }
    }
}
