package org.commonmark.test;

import org.commonmark.html.HtmlRenderer;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.parser.block.*;
import org.commonmark.spec.SpecReader;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ParserTest {

    @Test
    public void ioReaderTest() throws IOException {
        Parser parser = Parser.builder().build();

        InputStream input1 = SpecReader.getSpecInputStream();
        Node document1;
        try (InputStreamReader reader = new InputStreamReader(input1)) {
            document1 = parser.parseReader(reader);
        }

        String spec = SpecReader.readSpec();
        Node document2 = parser.parse(spec);

        HtmlRenderer renderer = HtmlRenderer.builder().escapeHtml(true).build();
        assertEquals(renderer.render(document2), renderer.render(document1));
    }

    @Test
    public void customBlockParserFactory() {
        Parser parser = Parser.builder().customBlockParserFactory(new DashBlockParserFactory()).build();

        // The dashes would normally be a HorizontalRule
        Node document = parser.parse("hey\n\n---\n");

        assertThat(document.getFirstChild(), instanceOf(Paragraph.class));
        assertEquals("hey", ((Text) document.getFirstChild().getFirstChild()).getLiteral());
        assertThat(document.getLastChild(), instanceOf(DashBlock.class));
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
    }

    private static class DashBlockParserFactory extends AbstractBlockParserFactory {

        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            if (state.getLine().equals("---")) {
                return BlockStart.of(new DashBlockParser());
            }
            return BlockStart.none();
        }
    }
}
