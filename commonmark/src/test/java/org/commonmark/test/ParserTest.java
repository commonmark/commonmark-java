package org.commonmark.test;

import org.commonmark.node.*;
import org.commonmark.parser.*;
import org.commonmark.parser.block.*;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.testutil.TestResources;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ParserTest {

    @Test
    public void ioReaderTest() throws IOException {
        Parser parser = Parser.builder().build();

        InputStream input1 = TestResources.getSpec().openStream();
        Node document1;
        try (InputStreamReader reader = new InputStreamReader(input1, StandardCharsets.UTF_8)) {
            document1 = parser.parseReader(reader);
        }

        String spec = TestResources.readAsString(TestResources.getSpec());
        Node document2 = parser.parse(spec);

        HtmlRenderer renderer = HtmlRenderer.builder().escapeHtml(true).build();
        assertThat(renderer.render(document1)).isEqualTo(renderer.render(document2));
    }

    @Test
    public void enabledBlockTypes() {
        String given = "# heading 1\n\nnot a heading";

        Parser parser = Parser.builder().build(); // all core parsers by default
        Node document = parser.parse(given);
        assertThat(document.getFirstChild()).isInstanceOf(Heading.class);

        Set<Class<? extends Block>> headersOnly = new HashSet<>();
        headersOnly.add(Heading.class);
        parser = Parser.builder().enabledBlockTypes(headersOnly).build();
        document = parser.parse(given);
        assertThat(document.getFirstChild()).isInstanceOf(Heading.class);

        Set<Class<? extends Block>> noCoreTypes = new HashSet<>();
        parser = Parser.builder().enabledBlockTypes(noCoreTypes).build();
        document = parser.parse(given);
        assertThat(document.getFirstChild()).isNotInstanceOf(Heading.class);
    }

    @Test
    public void enabledBlockTypesThrowsWhenGivenUnknownClass() {
        // BulletList can't be enabled separately at the moment, only all ListBlock types
        assertThatThrownBy(() ->
                Parser.builder().enabledBlockTypes(Set.of(Heading.class, BulletList.class)).build()).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void indentation() {
        String given = " - 1 space\n   - 3 spaces\n     - 5 spaces\n\t - tab + space";
        Parser parser = Parser.builder().build();
        Node document = parser.parse(given);

        assertThat(document.getFirstChild()).isInstanceOf(BulletList.class);

        Node list = document.getFirstChild(); // first level list
        assertThat(list.getLastChild()).as("expect one child").isEqualTo(list.getFirstChild());
        assertThat(firstText(list.getFirstChild())).isEqualTo("1 space");

        list = list.getFirstChild().getLastChild(); // second level list
        assertThat(list.getLastChild()).as("expect one child").isEqualTo(list.getFirstChild());
        assertThat(firstText(list.getFirstChild())).isEqualTo("3 spaces");

        list = list.getFirstChild().getLastChild(); // third level list
        assertThat(firstText(list.getFirstChild())).isEqualTo("5 spaces");
        assertThat(firstText(list.getFirstChild().getNext())).isEqualTo("tab + space");
    }

    @Test
    public void inlineParser() {
        final InlineParser fakeInlineParser = new InlineParser() {
            @Override
            public void parse(SourceLines lines, Node node) {
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

        assertThat(parser.parse(input).getFirstChild().getFirstChild()).isInstanceOf(ThematicBreak.class);
    }

    @Test
    public void threading() throws Exception {
        var parser = Parser.builder().build();
        var spec = TestResources.readAsString(TestResources.getSpec());

        var renderer = HtmlRenderer.builder().build();
        var expectedRendering = renderer.render(parser.parse(spec));

        // Parse in parallel using the same Parser instance.
        var futures = new ArrayList<Future<Node>>();
        var executorService = Executors.newFixedThreadPool(4);
        for (int i = 0; i < 40; i++) {
            var future = executorService.submit(() -> parser.parse(spec));
            futures.add(future);
        }

        for (var future : futures) {
            var node = future.get();
            assertThat(renderer.render(node)).isEqualTo(expectedRendering);
        }
    }

    private String firstText(Node n) {
        while (!(n instanceof Text)) {
            assertThat(n).isNotNull();
            n = n.getFirstChild();
        }
        return ((Text) n).getLiteral();
    }
}
