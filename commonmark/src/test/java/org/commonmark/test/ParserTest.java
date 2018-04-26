package org.commonmark.test;

import org.commonmark.node.*;
import org.commonmark.parser.InlineParser;
import org.commonmark.parser.InlineParserContext;
import org.commonmark.parser.InlineParserFactory;
import org.commonmark.parser.Parser;
import org.commonmark.parser.block.*;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.testutil.TestResources;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class ParserTest {

    @Test
    public void ioReaderTest() throws IOException {
        Parser parser = Parser.builder().build();

        InputStream input1 = TestResources.getSpec().openStream();
        Node document1;
        try (InputStreamReader reader = new InputStreamReader(input1, Charset.forName("UTF-8"))) {
            document1 = parser.parseReader(reader);
        }

        String spec = TestResources.readAsString(TestResources.getSpec());
        Node document2 = parser.parse(spec);

        HtmlRenderer renderer = HtmlRenderer.builder().escapeHtml(true).build();
        assertEquals(renderer.render(document2), renderer.render(document1));
    }

    @Test
    public void customBlockParserFactory() {
        Parser parser = Parser.builder().customBlockParserFactory(new DashBlockParserFactory()).build();

        // The dashes would normally be a ThematicBreak
        Node document = parser.parse("hey\n\n---\n");

        assertThat(document.getFirstChild(), instanceOf(Paragraph.class));
        assertEquals("hey", ((Text) document.getFirstChild().getFirstChild()).getLiteral());
        assertThat(document.getLastChild(), instanceOf(DashBlock.class));
    }

    @Test
    public void enabledBlockTypes() {
        String given = "# heading 1\n\nnot a heading";

        Parser parser = Parser.builder().build(); // all core parsers by default
        Node document = parser.parse(given);
        assertThat(document.getFirstChild(), instanceOf(Heading.class));

        Set<Class<? extends Block>> headersOnly = new HashSet<>();
        headersOnly.add(Heading.class);
        parser = Parser.builder().enabledBlockTypes(headersOnly).build();
        document = parser.parse(given);
        assertThat(document.getFirstChild(), instanceOf(Heading.class));

        Set<Class<? extends Block>> noCoreTypes = new HashSet<>();
        parser = Parser.builder().enabledBlockTypes(noCoreTypes).build();
        document = parser.parse(given);
        assertThat(document.getFirstChild(), not(instanceOf(Heading.class)));
    }

    @Test
    public void indentation() {
        String given = " - 1 space\n   - 3 spaces\n     - 5 spaces\n\t - tab + space";
        Parser parser = Parser.builder().build();
        Node document = parser.parse(given);

        assertThat(document.getFirstChild(), instanceOf(BulletList.class));

        Node list = document.getFirstChild(); // first level list
        assertEquals("expect one child", list.getFirstChild(), list.getLastChild());
        assertEquals("1 space", firstText(list.getFirstChild()));

        list = list.getFirstChild().getLastChild(); // second level list
        assertEquals("expect one child", list.getFirstChild(), list.getLastChild());
        assertEquals("3 spaces", firstText(list.getFirstChild()));

        list = list.getFirstChild().getLastChild(); // third level list
        assertEquals("5 spaces", firstText(list.getFirstChild()));
        assertEquals("tab + space", firstText(list.getFirstChild().getNext()));
    }

    @Test
    public void inlineParser() {
        final InlineParser fakeInlineParser = new InlineParser() {
            @Override
            public void parse(String input, Node node) {
                node.appendChild(new ThematicBreak());
            }
        };

        InlineParserFactory fakeInlineParserFactory = new InlineParserFactory() {

            @Override
            public InlineParser create(InlineParserContext inlineParserContext) {
                return fakeInlineParser;
            }
        };

        Parser parser = Parser.builder().inlineParserFactory(fakeInlineParserFactory).build();
        String input = "**bold** **bold** ~~strikethrough~~";

        assertThat(parser.parse(input).getFirstChild().getFirstChild(), instanceOf(ThematicBreak.class));
    }

    @Test
    public void threading() throws Exception {
        final Parser parser = Parser.builder().build();
        final String spec = TestResources.readAsString(TestResources.getSpec());

        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String expectedRendering = renderer.render(parser.parse(spec));

        // Parse in parallel using the same Parser instance.
        List<Future<Node>> futures = new ArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        for (int i = 0; i < 40; i++) {
            Future<Node> future = executorService.submit(new Callable<Node>() {
                @Override
                public Node call() throws Exception {
                    return parser.parse(spec);
                }
            });
            futures.add(future);
        }

        for (Future<Node> future : futures) {
            Node node = future.get();
            assertThat(renderer.render(node), is(expectedRendering));
        }
    }

    private String firstText(Node n) {
        while (!(n instanceof Text)) {
            assertThat(n, notNullValue());
            n = n.getFirstChild();
        }
        return ((Text) n).getLiteral();
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
