package org.commonmark.ext.front.matter;

import org.commonmark.Extension;
import org.commonmark.node.Document;
import org.commonmark.node.Node;
import org.commonmark.node.Paragraph;
import org.commonmark.node.Text;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.markdown.MarkdownRenderer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class YamlFrontMatterMarkdownRendererTest {

    private static final List<Extension> EXTENSIONS = List.of(YamlFrontMatterExtension.create());
    private static final Parser PARSER = Parser.builder().extensions(EXTENSIONS).build();
    private static final MarkdownRenderer RENDERER = MarkdownRenderer.builder().extensions(EXTENSIONS).build();

    // ===== Round-trip tests (parse string -> render -> compare to input) =====

    @Test
    public void testRoundTripSimple() {
        assertRoundTrip("---\ntitle: My Document\n---\n\nMarkdown content\n");
    }

    @Test
    public void testRoundTripEmptyValue() {
        assertRoundTrip("---\nkey:\n---\n\nMarkdown content\n");
    }

    @Test
    public void testRoundTripMultipleKeys() {
        assertRoundTrip("---\ntitle: My Document\nauthor: John Doe\n---\n\nMarkdown content\n");
    }

    @Test
    public void testRoundTripListValues() {
        assertRoundTrip("---\ntags:\n  - java\n  - markdown\n---\n\nMarkdown content\n");
    }

    @Test
    public void testRoundTripLiteralBlock() {
        assertRoundTrip("---\ndescription: |\n  first line\n  second line\n---\n\nMarkdown content\n");
    }

    @Test
    public void testRoundTripSingleQuotedValue() {
        assertRoundTrip("---\nkey: 'value with ''single quotes'''\n---\n\nMarkdown content\n");
    }

    @Test
    public void testRoundTripDoubleQuotedValue() {
        /*
         * NOTE: We don't know what the original escape character was and the markdown renderer always uses single
         * quote, hence why this technically doesn't round-trip.
         */
        var input = "---\nkey: \"value with \\\"double quotes\\\"\"\n---\n\nMarkdown content\n";
        var rendered = RENDERER.render(PARSER.parse(input));
        var expected = "---\nkey: 'value with \"double quotes\"'\n---\n\nMarkdown content\n";
        assertThat(rendered).isEqualTo(expected);
    }

    @Test
    public void testRoundTripFlowList() {
        // Flow-style list is stored as a single value - "[java, markdown]" - rendered back unquoted
        assertRoundTrip("---\ntags: [java, markdown]\n---\n\nMarkdown content\n");
    }

    @Test
    public void testRoundTripFlowMapping() {
        // Flow-style mapping is stored as a single value - "{key: value}" - rendered back unquoted
        assertRoundTrip("---\ndata: {key: value}\n---\n\nMarkdown content\n");
    }

    @Test
    public void testRoundTripEmptyFrontmatter() {
        assertRoundTrip("---\n---\n\nMarkdown content\n");
    }

    // ===== Programmatic construction tests =====

    @Test
    public void testProgrammaticallyBuilt() {
        var doc = buildDocumentWithFrontMatter(List.of(new YamlFrontMatterNode("title", List.of("My Document"))));

        assertRenderedEquals(doc, "---\ntitle: My Document\n---\n\nMarkdown content\n");
    }

    // ===== Quoting tests (values needing special treatment) =====

    @Test
    public void testValueWithColonSpace() {
        var doc = buildDocumentWithFrontMatter(List.of(new YamlFrontMatterNode("key", List.of("value with a: colon inside"))));

        assertRenderedEquals(doc, "---\nkey: 'value with a: colon inside'\n---\n\nMarkdown content\n");
    }

    @Test
    public void testValueWithColonNoSpace() {
        // Colon without trailing space is fine unquoted (e.g. timestamps, URLs)
        var doc = buildDocumentWithFrontMatter(List.of(new YamlFrontMatterNode("time", List.of("12:30:00"))));

        assertRenderedEquals(doc, "---\ntime: 12:30:00\n---\n\nMarkdown content\n");
    }

    @Test
    public void testValueStartingWithDash() {
        var doc = buildDocumentWithFrontMatter(List.of(new YamlFrontMatterNode("key", List.of("- not a list"))));

        assertRenderedEquals(doc, "---\nkey: '- not a list'\n---\n\nMarkdown content\n");
    }

    @Test
    public void testValueStartingWithUnmatchedBracket() {
        var doc = buildDocumentWithFrontMatter(List.of(new YamlFrontMatterNode("key", List.of("[broken"))));

        assertRenderedEquals(doc, "---\nkey: '[broken'\n---\n\nMarkdown content\n");
    }

    @Test
    public void testValueStartingWithMatchedBrackets() {
        // Valid flow list - should NOT be quoted
        var doc = buildDocumentWithFrontMatter(List.of(new YamlFrontMatterNode("flowList", List.of("[1, 2, 3]"))));

        assertRenderedEquals(doc, "---\nflowList: [1, 2, 3]\n---\n\nMarkdown content\n");
    }

    @Test
    public void testValueStartingWithUnmatchedBrace() {
        var doc = buildDocumentWithFrontMatter(List.of(new YamlFrontMatterNode("key", List.of("{broken"))));

        assertRenderedEquals(doc, "---\nkey: '{broken'\n---\n\nMarkdown content\n");
    }

    @Test
    public void testValueStartingWithMatchedBraces() {
        // Valid flow mapping - should NOT be quoted
        var doc = buildDocumentWithFrontMatter(List.of(new YamlFrontMatterNode("flowMapping", List.of("{key: val}"))));

        assertRenderedEquals(doc, "---\nflowMapping: {key: val}\n---\n\nMarkdown content\n");
    }

    @Test
    public void testValueContainingHashComment() {
        var doc = buildDocumentWithFrontMatter(List.of(new YamlFrontMatterNode("key", List.of("value # not a comment"))));

        assertRenderedEquals(doc, "---\nkey: 'value # not a comment'\n---\n\nMarkdown content\n");
    }

    @Test
    public void testValueContainingApostrophe() {
        var doc = buildDocumentWithFrontMatter(List.of(new YamlFrontMatterNode("key", List.of("it's a test"))));

        assertRenderedEquals(doc, "---\nkey: 'it''s a test'\n---\n\nMarkdown content\n");
    }

    @Test
    public void testEmptyStringValue() {
        var doc = buildDocumentWithFrontMatter(List.of(new YamlFrontMatterNode("empty", List.of(""))));

        assertRenderedEquals(doc, "---\nempty: ''\n---\n\nMarkdown content\n");
    }

    @Test
    public void testValueStartingWithDoubleQuote() {
        var doc = buildDocumentWithFrontMatter(List.of(new YamlFrontMatterNode("key", List.of("\"quotes within value\""))));

        assertRenderedEquals(doc, "---\nkey: '\"quotes within value\"'\n---\n\nMarkdown content\n");
    }

    private void assertRoundTrip(String input) {
        String rendered = RENDERER.render(PARSER.parse(input));
        assertThat(rendered).isEqualTo(input);
    }

    private void assertRenderedEquals(Node inputNode, String expectedOutput) {
        var renderedOutput = RENDERER.render(inputNode);
        assertThat(renderedOutput).isEqualTo(expectedOutput);
    }

    private Document buildDocumentWithFrontMatter(List<YamlFrontMatterNode> frontMatterNodes) {
        var doc = new Document();

        var frontmatter = new YamlFrontMatterBlock();
        for (var frontMatterNode : frontMatterNodes) {
            frontmatter.appendChild(frontMatterNode);
        }
        doc.appendChild(frontmatter);

        var para = new Paragraph();
        para.appendChild(new Text("Markdown content"));
        doc.appendChild(para);

        return doc;
    }
}
