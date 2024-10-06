package org.commonmark.test;

import org.commonmark.node.Link;
import org.commonmark.node.Node;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.text.LineBreakRendering;
import org.commonmark.renderer.text.TextContentNodeRendererContext;
import org.commonmark.renderer.text.TextContentNodeRendererFactory;
import org.commonmark.renderer.text.TextContentRenderer;
import org.commonmark.parser.Parser;
import org.commonmark.testutil.Asserts;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;

public class TextContentRendererTest {

    private static final Parser PARSER = Parser.builder().build();
    private static final TextContentRenderer COMPACT_RENDERER = TextContentRenderer.builder().build();
    private static final TextContentRenderer SEPARATE_RENDERER = TextContentRenderer.builder()
            .lineBreakRendering(LineBreakRendering.SEPARATE_BLOCKS).build();
    private static final TextContentRenderer STRIPPED_RENDERER = TextContentRenderer.builder().stripNewlines(true).build();

    @Test
    public void textContentText() {
        String s;

        s = "foo bar";
        assertCompact(s, "foo bar");
        assertStripped(s, "foo bar");

        s = "foo foo\n\nbar\nbar";
        assertCompact(s, "foo foo\nbar\nbar");
        assertSeparate(s, "foo foo\n\nbar\nbar");
        assertStripped(s, "foo foo bar bar");
    }

    @Test
    public void textContentHeading() {
        assertCompact("# Heading\n\nFoo", "Heading\nFoo");
        assertSeparate("# Heading\n\nFoo", "Heading\n\nFoo");
        assertStripped("# Heading\n\nFoo", "Heading: Foo");
    }

    @Test
    public void textContentEmphasis() {
        String s;
        String rendered;

        s = "***foo***";
        assertCompact(s, "foo");
        assertStripped(s, "foo");

        s = "foo ***foo*** bar ***bar***";
        assertCompact(s, "foo foo bar bar");
        assertStripped(s, "foo foo bar bar");

        s = "foo\n***foo***\nbar\n\n***bar***";
        assertCompact(s, "foo\nfoo\nbar\nbar");
        assertSeparate(s, "foo\nfoo\nbar\n\nbar");
        assertStripped(s, "foo foo bar bar");
    }

    @Test
    public void textContentQuotes() {
        String s;

        s = "foo\n>foo\nbar\n\nbar";
        assertCompact(s, "foo\n«foo\nbar»\nbar");
        assertSeparate(s, "foo\n\n«foo\nbar»\n\nbar");
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
        assertAll("foo ![text](http://link \"title\") bar", "foo \"text\" (title: http://link) bar");
        assertAll("foo ![text](http://link) bar", "foo \"text\" (http://link) bar");
        assertAll("foo ![text]() bar", "foo \"text\" bar");
    }

    @Test
    public void textContentLists() {
        String s;

        s = "foo\n* foo\n* bar\n\nbar";
        assertCompact(s, "foo\n* foo\n* bar\nbar");
        assertSeparate(s, "foo\n\n* foo\n* bar\n\nbar");
        assertStripped(s, "foo foo bar bar");

        s = "foo\n- foo\n- bar\n\nbar";
        assertCompact(s, "foo\n- foo\n- bar\nbar");
        assertSeparate(s, "foo\n\n- foo\n- bar\n\nbar");
        assertStripped(s, "foo foo bar bar");

        s = "foo\n1. foo\n2. bar\n\nbar";
        assertCompact(s, "foo\n1. foo\n2. bar\nbar");
        assertSeparate(s, "foo\n\n1. foo\n2. bar\n\nbar");
        assertStripped(s, "foo 1. foo 2. bar bar");

        s = "foo\n0) foo\n1) bar\n\nbar";
        assertCompact(s, "foo\n0) foo\n1) bar\nbar");
        assertSeparate(s, "foo\n0) foo\n\n1) bar\n\nbar");
        assertStripped(s, "foo 0) foo 1) bar bar");

        s = "bar\n1. foo\n   1. bar\n2. foo";
        assertCompact(s, "bar\n1. foo\n   1. bar\n2. foo");
        assertSeparate(s, "bar\n\n1. foo\n   1. bar\n2. foo");
        assertStripped(s, "bar 1. foo 1. bar 2. foo");

        s = "bar\n* foo\n   - bar\n* foo";
        assertCompact(s, "bar\n* foo\n   - bar\n* foo");
        assertSeparate(s, "bar\n\n* foo\n   - bar\n* foo");
        assertStripped(s, "bar foo bar foo");

        s = "bar\n* foo\n   1. bar\n   2. bar\n* foo";
        assertCompact(s, "bar\n* foo\n   1. bar\n   2. bar\n* foo");
        assertSeparate(s, "bar\n\n* foo\n   1. bar\n   2. bar\n* foo");
        assertStripped(s, "bar foo 1. bar 2. bar foo");

        s = "bar\n1. foo\n   * bar\n   * bar\n2. foo";
        assertCompact(s, "bar\n1. foo\n   * bar\n   * bar\n2. foo");
        assertSeparate(s, "bar\n\n1. foo\n   * bar\n   * bar\n2. foo");
        assertStripped(s, "bar 1. foo bar bar 2. foo");

        // For a loose list (not tight)
        s = "foo\n\n* bar\n\n* baz";
        // Compact ignores loose
        assertCompact(s, "foo\n* bar\n* baz");
        // Separate preserves it
        assertSeparate(s, "foo\n\n* bar\n\n* baz");
        assertStripped(s, "foo bar baz");

    }

    @Test
    public void textContentCode() {
        assertAll("foo `code` bar", "foo \"code\" bar");
    }

    @Test
    public void textContentCodeBlock() {
        String s;
        s = "foo\n```\nfoo\nbar\n```\nbar";
        assertCompact(s, "foo\nfoo\nbar\nbar");
        assertSeparate(s, "foo\n\nfoo\nbar\n\nbar");
        assertStripped(s, "foo foo bar bar");

        s = "foo\n\n    foo\n     bar\nbar";
        assertCompact(s, "foo\nfoo\n bar\nbar");
        assertSeparate(s, "foo\n\nfoo\n bar\n\nbar");
        assertStripped(s, "foo foo bar bar");
    }

    @Test
    public void textContentBreaks() {
        String s;

        s = "foo\nbar";
        assertCompact(s, "foo\nbar");
        assertSeparate(s, "foo\nbar");
        assertStripped(s, "foo bar");

        s = "foo  \nbar";
        assertCompact(s, "foo\nbar");
        assertSeparate(s, "foo\nbar");
        assertStripped(s, "foo bar");

        s = "foo\n___\nbar";
        assertCompact(s, "foo\n***\nbar");
        assertSeparate(s, "foo\n\n***\n\nbar");
        assertStripped(s, "foo bar");
    }

    @Test
    public void textContentHtml() {
        String html = "<table>\n" +
                "  <tr>\n" +
                "    <td>\n" +
                "           foobar\n" +
                "    </td>\n" +
                "  </tr>\n" +
                "</table>";
        assertCompact(html, html);
        assertSeparate(html, html);

        html = "foo <foo>foobar</foo> bar";
        assertAll(html, html);
    }

    @Test
    public void testOverrideNodeRendering() {
        var nodeRendererFactory = new TextContentNodeRendererFactory() {
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
        var renderer = TextContentRenderer.builder().nodeRendererFactory(nodeRendererFactory).build();
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
