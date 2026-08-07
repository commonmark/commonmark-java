package org.commonmark.test;

import java.util.Set;
import org.commonmark.node.Link;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.text.LineBreakRendering;
import org.commonmark.renderer.text.TextContentNodeRendererContext;
import org.commonmark.renderer.text.TextContentNodeRendererFactory;
import org.commonmark.renderer.text.TextContentRenderer;
import org.commonmark.testutil.Asserts;
import org.junit.jupiter.api.Test;

public class TextContentRendererTest {

    private static final Parser PARSER = Parser.builder().build();
    private static final TextContentRenderer COMPACT_RENDERER =
            TextContentRenderer.builder().build();
    private static final TextContentRenderer SEPARATE_RENDERER =
            TextContentRenderer.builder()
                    .lineBreakRendering(LineBreakRendering.SEPARATE_BLOCKS)
                    .build();
    private static final TextContentRenderer STRIPPED_RENDERER =
            TextContentRenderer.builder().lineBreakRendering(LineBreakRendering.STRIP).build();

    @Test
    public void textContentText() {
        String s;

        s = "foo bar";
        assertCompact(s, "foo bar");
        assertStripped(s, "foo bar");

        s =
                """
                foo foo

                bar
                bar""";
        assertCompact(
                s,
                """
                foo foo
                bar
                bar""");
        assertSeparate(
                s,
                """
                foo foo

                bar
                bar""");
        assertStripped(s, "foo foo bar bar");
    }

    @Test
    public void textContentHeading() {
        assertCompact(
                """
                # Heading

                Foo
                """,
                """
                        Heading
                        Foo""");
        assertSeparate(
                """
                # Heading

                Foo
                """,
                """
                        Heading

                        Foo""");
        assertStripped(
                """
                # Heading

                Foo
                """,
                "Heading: Foo");
    }

    @Test
    public void textContentEmphasis() {
        String s;

        s = "***foo***";
        assertCompact(s, "foo");
        assertStripped(s, "foo");

        s = "foo ***foo*** bar ***bar***";
        assertCompact(s, "foo foo bar bar");
        assertStripped(s, "foo foo bar bar");

        s =
                """
                foo
                ***foo***
                bar

                ***bar***
                """;
        assertCompact(
                s,
                """
                foo
                foo
                bar
                bar""");
        assertSeparate(
                s,
                """
                foo
                foo
                bar

                bar""");
        assertStripped(s, "foo foo bar bar");
    }

    @Test
    public void textContentQuotes() {
        String s;

        s =
                """
                foo
                >foo
                bar

                bar
                """;
        assertCompact(
                s,
                """
                foo
                «foo
                bar»
                bar""");
        assertSeparate(
                s,
                """
                foo

                «foo
                bar»

                bar""");
        assertStripped(s, "foo «foo bar» bar");
    }

    @Test
    public void textContentLinks() {
        assertAll("foo [text](http://link \"title\") bar", "foo \"text\" (title: http://link) bar");
        assertAll("foo [text](http://link \"http://link\") bar", "foo \"text\" (http://link) bar");
        assertAll("foo [text](http://link) bar", "foo \"text\" (http://link) bar");
        assertAll("foo [text]() bar", "foo \"text\" bar");
        assertAll("foo http://link bar", "foo http://link bar");
    }

    @Test
    public void textContentImages() {
        assertAll(
                "foo ![text](http://link \"title\") bar", "foo \"text\" (title: http://link) bar");
        assertAll("foo ![text](http://link) bar", "foo \"text\" (http://link) bar");
        assertAll("foo ![text]() bar", "foo \"text\" bar");
    }

    @Test
    public void textContentLists() {
        String s;

        s =
                """
                foo
                * foo
                * bar

                bar""";
        assertCompact(
                s,
                """
                foo
                * foo
                * bar
                bar""");
        assertSeparate(
                s,
                """
                foo

                * foo
                * bar

                bar""");
        assertStripped(s, "foo foo bar bar");

        s =
                """
                foo
                - foo
                - bar

                bar
                """;
        assertCompact(
                s,
                """
                foo
                - foo
                - bar
                bar""");
        assertSeparate(
                s,
                """
                foo

                - foo
                - bar

                bar""");
        assertStripped(s, "foo foo bar bar");

        s =
                """
                foo
                1. foo
                2. bar

                bar
                """;
        assertCompact(
                s,
                """
                foo
                1. foo
                2. bar
                bar""");
        assertSeparate(
                s,
                """
                foo

                1. foo
                2. bar

                bar""");
        assertStripped(s, "foo 1. foo 2. bar bar");

        s =
                """
                foo
                0) foo
                1) bar

                bar
                """;
        assertCompact(
                s,
                """
                foo
                0) foo
                1) bar
                bar""");
        assertSeparate(
                s,
                """
                foo
                0) foo

                1) bar

                bar""");
        assertStripped(s, "foo 0) foo 1) bar bar");

        s =
                """
                bar
                1. foo
                   1. bar
                2. foo
                """;
        assertCompact(
                s,
                """
                bar
                1. foo
                   1. bar
                2. foo""");
        assertSeparate(
                s,
                """
                bar

                1. foo
                   1. bar
                2. foo""");
        assertStripped(s, "bar 1. foo 1. bar 2. foo");

        s =
                """
                bar
                * foo
                  - bar
                * foo
                """;
        assertCompact(
                s,
                """
                bar
                * foo
                  - bar
                * foo""");
        assertSeparate(
                s,
                """
                bar

                * foo
                  - bar
                * foo""");
        assertStripped(s, "bar foo bar foo");

        s =
                """
                bar
                * foo
                  1. bar
                  2. bar
                * foo
                """;
        assertCompact(
                s,
                """
                bar
                * foo
                  1. bar
                  2. bar
                * foo""");
        assertSeparate(
                s,
                """
                bar

                * foo
                  1. bar
                  2. bar
                * foo""");
        assertStripped(s, "bar foo 1. bar 2. bar foo");

        s =
                """
                bar
                1. foo
                   * bar
                   * bar
                2. foo
                """;
        assertCompact(
                s,
                """
                bar
                1. foo
                   * bar
                   * bar
                2. foo""");
        assertSeparate(
                s,
                """
                bar

                1. foo
                   * bar
                   * bar
                2. foo""");
        assertStripped(s, "bar 1. foo bar bar 2. foo");

        // For a loose list (not tight)
        s =
                """
                foo

                * bar

                * baz
                """;
        // Compact ignores loose
        assertCompact(
                s,
                """
                foo
                * bar
                * baz""");
        // Separate preserves it
        assertSeparate(
                s,
                """
                foo

                * bar

                * baz""");
        assertStripped(s, "foo bar baz");
    }

    @Test
    public void textContentCode() {
        assertAll("foo `code` bar", "foo \"code\" bar");
    }

    @Test
    public void textContentCodeBlock() {
        String s;
        s =
                """
                foo
                ```
                foo
                bar
                ```
                bar
                """;
        assertCompact(s, "foo\nfoo\nbar\nbar");
        assertSeparate(s, "foo\n\nfoo\nbar\n\nbar");
        assertStripped(s, "foo foo bar bar");

        s =
                """
                foo

                    foo
                     bar
                bar
                """;
        assertCompact(
                s,
                """
                foo
                foo
                 bar
                bar""");
        assertSeparate(
                s,
                """
                foo

                foo
                 bar

                bar""");
        assertStripped(s, "foo foo bar bar");
    }

    @Test
    public void textContentBreaks() {
        String s;

        s =
                """
                foo
                bar
                """;
        assertCompact(
                s,
                """
                foo
                bar""");
        assertSeparate(
                s,
                """
                foo
                bar""");
        assertStripped(s, "foo bar");

        s =
                """
                foo \s
                bar
                """;
        assertCompact(
                s,
                """
                foo
                bar""");
        assertSeparate(
                s,
                """
                foo
                bar""");
        assertStripped(s, "foo bar");

        s =
                """
                foo
                ___
                bar
                """;
        assertCompact(
                s,
                """
                foo
                ***
                bar""");
        assertSeparate(
                s,
                """
                foo

                ***

                bar""");
        assertStripped(s, "foo bar");
    }

    @Test
    public void textContentHtml() {
        String html =
                """
                <table>
                  <tr>
                    <td>
                           foobar
                    </td>
                  </tr>
                </table>""";
        assertCompact(html, html);
        assertSeparate(html, html);

        html = "foo <foo>foobar</foo> bar";
        assertAll(html, html);
    }

    @Test
    public void testContentNestedLists() {
        var s =
                """
                List:
                1. 2) 3.\s
                end""";
        assertCompact(s, s);

        var s2 =
                """
                1. A
                   1) B
                      1. Test""";
        assertCompact(s2, s2);
    }

    @Test
    public void testOverrideNodeRendering() {
        var nodeRendererFactory =
                new TextContentNodeRendererFactory() {
                    @Override
                    public NodeRenderer create(TextContentNodeRendererContext context) {
                        return new NodeRenderer() {

                            @Override
                            public Set<Class<? extends Node>> getNodeTypes() {
                                return Set.of(Link.class);
                            }

                            @Override
                            public void render(Node node) {
                                context.getWriter().write('"');
                                renderChildren(node);
                                context.getWriter().write('"');
                            }

                            private void renderChildren(Node parent) {
                                Node node = parent.getFirstChild();
                                while (node != null) {
                                    Node next = node.getNext();
                                    context.render(node);
                                    node = next;
                                }
                            }
                        };
                    }
                };
        var renderer =
                TextContentRenderer.builder().nodeRendererFactory(nodeRendererFactory).build();
        var source = "Hi [Example](https://example.com)";
        Asserts.assertRendering(source, "Hi \"Example\"", renderer.render(PARSER.parse(source)));
    }

    private void assertCompact(String source, String expected) {
        var doc = PARSER.parse(source);
        var actualRendering = COMPACT_RENDERER.render(doc);
        Asserts.assertRendering(source, expected, actualRendering);
    }

    private void assertSeparate(String source, String expected) {
        var doc = PARSER.parse(source);
        var actualRendering = SEPARATE_RENDERER.render(doc);
        Asserts.assertRendering(source, expected, actualRendering);
    }

    private void assertStripped(String source, String expected) {
        var doc = PARSER.parse(source);
        var actualRendering = STRIPPED_RENDERER.render(doc);
        Asserts.assertRendering(source, expected, actualRendering);
    }

    private void assertAll(String source, String expected) {
        assertCompact(source, expected);
        assertSeparate(source, expected);
        assertStripped(source, expected);
    }
}
