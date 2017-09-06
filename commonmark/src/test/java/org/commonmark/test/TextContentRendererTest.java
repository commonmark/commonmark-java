package org.commonmark.test;

import org.commonmark.renderer.text.TextContentRenderer;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TextContentRendererTest {

    @Test
    public void textContentText() {
        String source;
        String rendered;

        source = "foo bar";
        rendered = defaultRenderer().render(parse(source));
        assertEquals("foo bar", rendered);
        rendered = strippedRenderer().render(parse(source));
        assertEquals("foo bar", rendered);

        source = "foo foo\n\nbar\nbar";
        rendered = defaultRenderer().render(parse(source));
        assertEquals("foo foo\nbar\nbar", rendered);
        rendered = strippedRenderer().render(parse(source));
        assertEquals("foo foo bar bar", rendered);
    }

    @Test
    public void textContentEmphasis() {
        String source;
        String rendered;

        source = "***foo***";
        rendered = defaultRenderer().render(parse(source));
        assertEquals("foo", rendered);
        rendered = strippedRenderer().render(parse(source));
        assertEquals("foo", rendered);

        source = "foo ***foo*** bar ***bar***";
        rendered = defaultRenderer().render(parse(source));
        assertEquals("foo foo bar bar", rendered);
        rendered = strippedRenderer().render(parse(source));
        assertEquals("foo foo bar bar", rendered);

        source = "foo\n***foo***\nbar\n\n***bar***";
        rendered = defaultRenderer().render(parse(source));
        assertEquals("foo\nfoo\nbar\nbar", rendered);
        rendered = strippedRenderer().render(parse(source));
        assertEquals("foo foo bar bar", rendered);
    }

    @Test
    public void textContentQuotes() {
        String source;
        String rendered;

        source = "foo\n>foo\nbar\n\nbar";
        rendered = defaultRenderer().render(parse(source));
        assertEquals("foo\n«foo\nbar»\nbar", rendered);
        rendered = strippedRenderer().render(parse(source));
        assertEquals("foo «foo bar» bar", rendered);
    }

    @Test
    public void textContentLinks() {
        String source;
        String expected;
        String rendered;

        source = "foo [text](http://link \"title\") bar";
        expected = "foo \"text\" (title: http://link) bar";
        rendered = defaultRenderer().render(parse(source));
        assertEquals(expected, rendered);
        rendered = strippedRenderer().render(parse(source));
        assertEquals(expected, rendered);

        source = "foo [text](http://link \"http://link\") bar";
        expected = "foo \"text\" (http://link) bar";
        rendered = defaultRenderer().render(parse(source));
        assertEquals(expected, rendered);
        rendered = strippedRenderer().render(parse(source));
        assertEquals(expected, rendered);

        source = "foo [text](http://link) bar";
        expected = "foo \"text\" (http://link) bar";
        rendered = defaultRenderer().render(parse(source));
        assertEquals(expected, rendered);
        rendered = strippedRenderer().render(parse(source));
        assertEquals(expected, rendered);

        source = "foo [text]() bar";
        expected = "foo \"text\" bar";
        rendered = defaultRenderer().render(parse(source));
        assertEquals(expected, rendered);
        rendered = strippedRenderer().render(parse(source));
        assertEquals(expected, rendered);

        source = "foo http://link bar";
        expected = "foo http://link bar";
        rendered = defaultRenderer().render(parse(source));
        assertEquals(expected, rendered);
        rendered = strippedRenderer().render(parse(source));
        assertEquals(expected, rendered);
    }

    @Test
    public void textContentImages() {
        String source;
        String expected;
        String rendered;

        source = "foo ![text](http://link \"title\") bar";
        expected = "foo \"text\" (title: http://link) bar";
        rendered = defaultRenderer().render(parse(source));
        assertEquals(expected, rendered);
        rendered = strippedRenderer().render(parse(source));
        assertEquals(expected, rendered);

        source = "foo ![text](http://link) bar";
        expected = "foo \"text\" (http://link) bar";
        rendered = defaultRenderer().render(parse(source));
        assertEquals(expected, rendered);
        rendered = strippedRenderer().render(parse(source));
        assertEquals(expected, rendered);

        source = "foo ![text]() bar";
        expected = "foo \"text\" bar";
        rendered = defaultRenderer().render(parse(source));
        assertEquals(expected, rendered);
        rendered = strippedRenderer().render(parse(source));
        assertEquals(expected, rendered);
    }

    @Test
    public void textContentLists() {
        String source;
        String rendered;

        source = "foo\n* foo\n* bar\n\nbar";
        rendered = defaultRenderer().render(parse(source));
        assertEquals("foo\n* foo\n* bar\nbar", rendered);
        rendered = strippedRenderer().render(parse(source));
        assertEquals("foo foo bar bar", rendered);

        source = "foo\n- foo\n- bar\n\nbar";
        rendered = defaultRenderer().render(parse(source));
        assertEquals("foo\n- foo\n- bar\nbar", rendered);
        rendered = strippedRenderer().render(parse(source));
        assertEquals("foo foo bar bar", rendered);

        source = "foo\n1. foo\n2. bar\n\nbar";
        rendered = defaultRenderer().render(parse(source));
        assertEquals("foo\n1. foo\n2. bar\nbar", rendered);
        rendered = strippedRenderer().render(parse(source));
        assertEquals("foo 1. foo 2. bar bar", rendered);

        source = "foo\n0) foo\n1) bar\n\nbar";
        rendered = defaultRenderer().render(parse(source));
        assertEquals("foo\n0) foo\n1) bar\nbar", rendered);
        rendered = strippedRenderer().render(parse(source));
        assertEquals("foo 0) foo 1) bar bar", rendered);

        source = "bar\n1. foo\n   1. bar\n2. foo";
        rendered = defaultRenderer().render(parse(source));
        assertEquals("bar\n1. foo\n   1. bar\n2. foo", rendered);
        rendered = strippedRenderer().render(parse(source));
        assertEquals("bar 1. foo 1. bar 2. foo", rendered);

        source = "bar\n* foo\n   - bar\n* foo";
        rendered = defaultRenderer().render(parse(source));
        assertEquals("bar\n* foo\n   - bar\n* foo", rendered);
        rendered = strippedRenderer().render(parse(source));
        assertEquals("bar foo bar foo", rendered);

        source = "bar\n* foo\n   1. bar\n   2. bar\n* foo";
        rendered = defaultRenderer().render(parse(source));
        assertEquals("bar\n* foo\n   1. bar\n   2. bar\n* foo", rendered);
        rendered = strippedRenderer().render(parse(source));
        assertEquals("bar foo 1. bar 2. bar foo", rendered);

        source = "bar\n1. foo\n   * bar\n   * bar\n2. foo";
        rendered = defaultRenderer().render(parse(source));
        assertEquals("bar\n1. foo\n   * bar\n   * bar\n2. foo", rendered);
        rendered = strippedRenderer().render(parse(source));
        assertEquals("bar 1. foo bar bar 2. foo", rendered);
    }

    @Test
    public void textContentCode() {
        String source;
        String expected;
        String rendered;

        source = "foo `code` bar";
        expected = "foo \"code\" bar";
        rendered = defaultRenderer().render(parse(source));
        assertEquals(expected, rendered);
        rendered = strippedRenderer().render(parse(source));
        assertEquals(expected, rendered);
    }

    @Test
    public void textContentCodeBlock() {
        String source;
        String rendered;

        source = "foo\n```\nfoo\nbar\n```\nbar";
        rendered = defaultRenderer().render(parse(source));
        assertEquals("foo\nfoo\nbar\nbar", rendered);
        rendered = strippedRenderer().render(parse(source));
        assertEquals("foo foo bar bar", rendered);

        source = "foo\n\n    foo\n     bar\nbar";
        rendered = defaultRenderer().render(parse(source));
        assertEquals("foo\nfoo\n bar\nbar", rendered);
        rendered = strippedRenderer().render(parse(source));
        assertEquals("foo foo bar bar", rendered);
    }

    @Test
    public void textContentBrakes() {
        String source;
        String rendered;

        source = "foo\nbar";
        rendered = defaultRenderer().render(parse(source));
        assertEquals("foo\nbar", rendered);
        rendered = strippedRenderer().render(parse(source));
        assertEquals("foo bar", rendered);

        source = "foo  \nbar";
        rendered = defaultRenderer().render(parse(source));
        assertEquals("foo\nbar", rendered);
        rendered = strippedRenderer().render(parse(source));
        assertEquals("foo bar", rendered);

        source = "foo\n___\nbar";
        rendered = defaultRenderer().render(parse(source));
        assertEquals("foo\n***\nbar", rendered);
        rendered = strippedRenderer().render(parse(source));
        assertEquals("foo bar", rendered);
    }

    @Test
    public void textContentHtml() {
        String rendered;

        String html = "<table>\n" +
                "  <tr>\n" +
                "    <td>\n" +
                "           foobar\n" +
                "    </td>\n" +
                "  </tr>\n" +
                "</table>";
        rendered = defaultRenderer().render(parse(html));
        assertEquals(html, rendered);

        html = "foo <foo>foobar</foo> bar";
        rendered = defaultRenderer().render(parse(html));
        assertEquals(html, rendered);
    }

    private TextContentRenderer defaultRenderer() {
        return TextContentRenderer.builder().build();
    }

    private TextContentRenderer strippedRenderer() {
        return TextContentRenderer.builder().stripNewlines(true).build();
    }

    private Node parse(String source) {
        return Parser.builder().build().parse(source);
    }
}
