package org.commonmark.test;

import org.commonmark.renderer.text.TextContentRenderer;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TextContentRendererTest {

    @Test
    public void textContentEmphasis() {
        String rendered;

        rendered = defaultRenderer().render(parse("foo\n***foo***\nbar\n\n***bar***"));
        assertEquals("foo\nfoo\nbar\nbar", rendered);

        rendered = strippedRenderer().render(parse("foo\n***foo\nbar***\n\n***bar***"));
        assertEquals("foo foo bar bar", rendered);
    }

    @Test
    public void textContentQuotes() {
        String rendered;

        rendered = defaultRenderer().render(parse("foo\n>foo\nbar\n\nbar"));
        assertEquals("foo\n«foo\nbar»\nbar", rendered);

        rendered = strippedRenderer().render(parse("foo\n>foo\nbar\n\nbar"));
        assertEquals("foo «foo bar» bar", rendered);
    }

    @Test
    public void textContentLinks() {
        String rendered;

        rendered = defaultRenderer().render(parse("foo [text](http://link \"title\") bar"));
        assertEquals("foo \"text\" (title: http://link) bar", rendered);

        rendered = defaultRenderer().render(parse("foo [text](http://link) bar"));
        assertEquals("foo \"text\" (http://link) bar", rendered);

        rendered = defaultRenderer().render(parse("foo [text]() bar"));
        assertEquals("foo \"text\" bar", rendered);

        rendered = defaultRenderer().render(parse("foo http://link bar"));
        assertEquals("foo http://link bar", rendered);
    }

    @Test
    public void textContentImages() {
        String rendered;

        rendered = defaultRenderer().render(parse("foo ![text](http://link \"title\") bar"));
        assertEquals("foo \"text\" (title: http://link) bar", rendered);

        rendered = defaultRenderer().render(parse("foo ![text](http://link) bar"));
        assertEquals("foo \"text\" (http://link) bar", rendered);

        rendered = defaultRenderer().render(parse("foo ![text]() bar"));
        assertEquals("foo \"text\" bar", rendered);
    }

    @Test
    public void textContentLists() {
        String rendered;

        rendered = defaultRenderer().render(parse("foo\n* foo\n* bar\n\nbar"));
        assertEquals("foo\n* foo\n* bar\nbar", rendered);

        rendered = defaultRenderer().render(parse("foo\n- foo\n- bar\n\nbar"));
        assertEquals("foo\n- foo\n- bar\nbar", rendered);

        rendered = strippedRenderer().render(parse("foo\n* foo\n* bar\n\nbar"));
        assertEquals("foo foo bar bar", rendered);

        rendered = defaultRenderer().render(parse("foo\n1. foo\n2. bar\n\nbar"));
        assertEquals("foo\n1. foo\n2. bar\nbar", rendered);

        rendered = defaultRenderer().render(parse("foo\n0) foo\n1) bar\n\nbar"));
        assertEquals("foo\n0) foo\n1) bar\nbar", rendered);

        rendered = strippedRenderer().render(parse("foo\n1. foo\n2. bar\n\nbar"));
        assertEquals("foo 1. foo 2. bar bar", rendered);

        rendered = strippedRenderer().render(parse("foo\n0) foo\n1) bar\n\nbar"));
        assertEquals("foo 0) foo 1) bar bar", rendered);
    }

    @Test
    public void textContentCode() {
        String rendered;

        rendered = defaultRenderer().render(parse("foo `code` bar"));
        assertEquals("foo \"code\" bar", rendered);
    }

    @Test
    public void textContentCodeBlock() {
        String rendered;

        rendered = defaultRenderer().render(parse("foo\n```\nfoo\nbar\n```\nbar"));
        assertEquals("foo\nfoo\nbar\nbar", rendered);

        rendered = strippedRenderer().render(parse("foo\n```\nfoo\nbar\n```\nbar"));
        assertEquals("foo foo bar bar", rendered);

        rendered = defaultRenderer().render(parse("foo\n\n    foo\n     bar\nbar"));
        assertEquals("foo\nfoo\n bar\nbar", rendered);

        rendered = strippedRenderer().render(parse("foo\n\n    foo\n     bar\nbar"));
        assertEquals("foo foo bar bar", rendered);
    }

    @Test
    public void textContentBrakes() {
        String rendered;

        rendered = defaultRenderer().render(parse("foo\nbar"));
        assertEquals("foo\nbar", rendered);

        rendered = strippedRenderer().render(parse("foo\nbar"));
        assertEquals("foo bar", rendered);

        rendered = defaultRenderer().render(parse("foo  \nbar"));
        assertEquals("foo\nbar", rendered);

        rendered = strippedRenderer().render(parse("foo  \nbar"));
        assertEquals("foo bar", rendered);

        rendered = defaultRenderer().render(parse("foo\n___\nbar"));
        assertEquals("foo\n***\nbar", rendered);

        rendered = strippedRenderer().render(parse("foo\n___\nbar"));
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
