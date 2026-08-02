package org.commonmark.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.commonmark.node.*;
import org.commonmark.parser.*;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.markdown.MarkdownRenderer;
import org.commonmark.testutil.TestResources;
import org.junit.jupiter.api.Test;

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
        assertThatThrownBy(
                        () ->
                                Parser.builder()
                                        .enabledBlockTypes(Set.of(Heading.class, BulletList.class))
                                        .build())
                .isInstanceOf(IllegalArgumentException.class);
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
        final InlineParser fakeInlineParser =
                (lines, node) -> node.appendChild(new ThematicBreak());

        InlineParserFactory fakeInlineParserFactory = inlineParserContext -> fakeInlineParser;

        Parser parser = Parser.builder().inlineParserFactory(fakeInlineParserFactory).build();
        String input = "**bold** **bold** ~~strikethrough~~";

        assertThat(parser.parse(input).getFirstChild().getFirstChild())
                .isInstanceOf(ThematicBreak.class);
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

    @Test
    public void maxOpenBlockParsersMustBeZeroOrGreater() {
        assertThatThrownBy(() -> Parser.builder().maxOpenBlockParsers(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void maxOpenBlockParsersIsOptIn() {
        var parser = Parser.builder().build();

        var document = parser.parse(alternatingNestedList(9));

        assertThat(renderText(deepestStructuredParagraph(document, 9))).isEqualTo("level9");
    }

    @Test
    public void maxOpenBlockParsersPreservesSevenLogicalListLevelsAtSeventeenBlocks() {
        var parser = Parser.builder().maxOpenBlockParsers(17).build();

        var document = parser.parse(alternatingNestedList(7));

        assertThat(renderText(deepestStructuredParagraph(document, 7))).isEqualTo("level7");
    }

    @Test
    public void maxOpenBlockParsersPreservesEightLogicalListLevelsAtSeventeenBlocks() {
        var parser = Parser.builder().maxOpenBlockParsers(17).build();

        var document = parser.parse(alternatingNestedList(8));

        assertThat(renderText(deepestStructuredParagraph(document, 8))).isEqualTo("level8");
    }

    @Test
    public void maxOpenBlockParsersDegradesTheNinthLogicalListLevelToPlainText() {
        var parser = Parser.builder().maxOpenBlockParsers(17).build();

        var document = parser.parse(alternatingNestedList(9));
        var deepestParagraph = deepestStructuredParagraph(document, 8);

        assertThat(renderText(deepestParagraph)).isEqualTo("level8\n\\- level9");
        assertThat(deepestParagraph.getNext()).isNull();
    }

    @Test
    public void maxOpenBlockParsersAlsoLimitsMixedListAndBlockQuoteNesting() {
        var parser = Parser.builder().maxOpenBlockParsers(5).build();

        var document =
                parser.parse(
                        String.join(
                                "\n", "- level1", "  > level2", "  > > level3", "  > > > level4"));

        var listBlock = document.getFirstChild();
        assertThat(listBlock).isInstanceOf(BulletList.class);

        var listItem = listBlock.getFirstChild();
        var blockQuote1 = listItem.getLastChild();
        assertThat(blockQuote1).isInstanceOf(BlockQuote.class);

        var blockQuote2 = blockQuote1.getLastChild();
        assertThat(blockQuote2).isInstanceOf(BlockQuote.class);

        var deepestParagraph = blockQuote2.getLastChild();
        assertThat(deepestParagraph).isInstanceOf(Paragraph.class);
        assertThat(renderText(deepestParagraph)).isEqualTo("level3\n\\> level4");
        assertThat(deepestParagraph.getNext()).isNull();
    }

    private String firstText(Node n) {
        while (!(n instanceof Text)) {
            assertThat(n).isNotNull();
            n = n.getFirstChild();
        }
        return ((Text) n).getLiteral();
    }

    private Paragraph deepestStructuredParagraph(Node document, int levels) {
        Node node = document.getFirstChild();
        for (int level = 1; level <= levels; level++) {
            assertThat(node).isInstanceOf(ListBlock.class);
            var listItem = node.getFirstChild();
            assertThat(listItem).isNotNull();
            if (level == levels) {
                assertThat(listItem.getFirstChild()).isInstanceOf(Paragraph.class);
                return (Paragraph) listItem.getFirstChild();
            }
            node = listItem.getLastChild();
        }
        throw new AssertionError("unreachable");
    }

    @Test
    public void maxInlineNestingMustBeZeroOrGreater() {
        assertThatThrownBy(() -> Parser.builder().maxInlineNesting(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void maxInlineNestingLimitsEmphasisNestingDepth() {
        var parser = Parser.builder().maxInlineNesting(3).build();

        var document = parser.parse("*".repeat(20) + "x" + "*".repeat(20));

        var paragraph = document.getFirstChild();
        assertThat(paragraph).isInstanceOf(Paragraph.class);

        // Leftover, un-nested delimiters remain as literal text either side.
        assertThat(paragraph.getFirstChild()).isInstanceOf(Text.class);
        assertThat(((Text) paragraph.getFirstChild()).getLiteral()).isEqualTo("*".repeat(14));
        assertThat(paragraph.getLastChild()).isInstanceOf(Text.class);
        assertThat(((Text) paragraph.getLastChild()).getLiteral()).isEqualTo("*".repeat(14));

        // Nesting itself is capped at the configured depth.
        var depth = 0;
        var node = paragraph.getFirstChild().getNext();
        while (node instanceof StrongEmphasis) {
            depth++;
            node = node.getFirstChild();
        }
        assertThat(depth).isEqualTo(3);
        assertThat(node).isInstanceOf(Text.class);
        assertThat(((Text) node).getLiteral()).isEqualTo("x");
    }

    @Test
    public void maxInlineNestingZeroTreatsAllInlineStructureAsText() {
        var parser = Parser.builder().maxInlineNesting(0).build();
        var renderer = HtmlRenderer.builder().build();

        assertThat(renderer.render(parser.parse("*a*"))).isEqualTo("<p>*a*</p>\n");
        assertThat(renderer.render(parser.parse("[a](u)"))).isEqualTo("<p>[a](u)</p>\n");
        assertThat(renderer.render(parser.parse("![a](u)"))).isEqualTo("<p>![a](u)</p>\n");
    }

    @Test
    public void maxInlineNestingTreatsBracketsOverLimitAsText() {
        var parser = Parser.builder().maxInlineNesting(1).build();
        var renderer = HtmlRenderer.builder().build();

        // The inner image is within the limit; the outer one would nest a level deeper, so its
        // brackets are treated as plain text.
        assertThat(renderer.render(parser.parse("![![a](u)](v)")))
                .isEqualTo("<p>![<img src=\"u\" alt=\"a\" />](v)</p>\n");
    }

    @Test
    public void maxInlineNestingCanBeExceededByOneLevelInsideBrackets() {
        // The limit is checked when the closing bracket is parsed, but emphasis inside the
        // brackets is only processed after that, so it isn't counted for the link itself (only for
        // anything wrapping the link). That means brackets can exceed the limit by one level:
        // documenting, not prescribing, this behavior. Note the asymmetry with emphasis on the
        // outside, where the link's depth is visible and the emphasis is treated as text.
        var parser = Parser.builder().maxInlineNesting(1).build();
        var renderer = HtmlRenderer.builder().build();

        assertThat(renderer.render(parser.parse("[*a*](u)")))
                .isEqualTo("<p><a href=\"u\"><em>a</em></a></p>\n");
        assertThat(renderer.render(parser.parse("*[a](u)*")))
                .isEqualTo("<p>*<a href=\"u\">a</a>*</p>\n");
    }

    @Test
    public void deeplyNestedInlineDoesNotOverflowParsing() {
        // Deeply nested inline emphasis previously overflowed the stack during inline
        // post-processing: mergeChildTextNodes recursed once per nesting level. Parsing must
        // complete without a StackOverflowError. maxInlineNesting is disabled here (it would
        // otherwise cap nesting depth) since this test is specifically about the text-merging
        // step, not about the nesting limit itself.
        var n = 50_000;
        var md = "*".repeat(n) + "x" + "*".repeat(n);

        var document = Parser.builder().maxInlineNesting(Integer.MAX_VALUE).build().parse(md);

        // Confirm the tree really is deeply nested, descending iteratively (no recursion).
        var nesting = 0;
        for (var node = document; node.getFirstChild() != null; node = node.getFirstChild()) {
            nesting++;
        }
        assertThat(nesting).isGreaterThan(1000);
    }

    private String renderText(Node node) {
        return MarkdownRenderer.builder().build().render(node).trim();
    }

    private String alternatingNestedList(int levels) {
        int indent = 0;
        var lines = new ArrayList<String>();
        for (int level = 1; level <= levels; level++) {
            var ordered = level % 2 == 0;
            var marker = ordered ? "1. " : "- ";
            lines.add(" ".repeat(indent) + marker + "level" + level);
            indent += marker.length();
        }
        return String.join("\n", lines);
    }

    private int depth(Node node) {
        int depth = 0;
        while (node.getParent() != null) {
            node = node.getParent();
            depth++;
        }
        return depth;
    }
}
