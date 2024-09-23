package org.commonmark.test;

import org.commonmark.renderer.text.TextContentRenderer;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.testutil.Asserts;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TextContentRendererTest {

    @Test
    public void textContentText() {
        String s;

        s = "foo bar";
        assertCompact(s, "foo bar");
        assertStripped(s, "foo bar");

        s = "foo foo\n\nbar\nbar";
        assertCompact(s, "foo foo\nbar\nbar");
        assertStripped(s, "foo foo bar bar");
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
        assertStripped(s, "foo foo bar bar");
    }

    @Test
    public void textContentQuotes() {
        String s;

        s = "foo\n>foo\nbar\n\nbar";
        assertCompact(s, "foo\n«foo\nbar»\nbar");
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
        assertStripped(s, "foo foo bar bar");

        s = "foo\n- foo\n- bar\n\nbar";
        assertCompact(s, "foo\n- foo\n- bar\nbar");
        assertStripped(s, "foo foo bar bar");

        s = "foo\n1. foo\n2. bar\n\nbar";
        assertCompact(s, "foo\n1. foo\n2. bar\nbar");
        assertStripped(s, "foo 1. foo 2. bar bar");

        s = "foo\n0) foo\n1) bar\n\nbar";
        assertCompact(s, "foo\n0) foo\n1) bar\nbar");
        assertStripped(s, "foo 0) foo 1) bar bar");

        s = "bar\n1. foo\n   1. bar\n2. foo";
        assertCompact(s, "bar\n1. foo\n   1. bar\n2. foo");
        assertStripped(s, "bar 1. foo 1. bar 2. foo");

        s = "bar\n* foo\n   - bar\n* foo";
        assertCompact(s, "bar\n* foo\n   - bar\n* foo");
        assertStripped(s, "bar foo bar foo");

        s = "bar\n* foo\n   1. bar\n   2. bar\n* foo";
        assertCompact(s, "bar\n* foo\n   1. bar\n   2. bar\n* foo");
        assertStripped(s, "bar foo 1. bar 2. bar foo");

        s = "bar\n1. foo\n   * bar\n   * bar\n2. foo";
        assertCompact(s, "bar\n1. foo\n   * bar\n   * bar\n2. foo");
        assertStripped(s, "bar 1. foo bar bar 2. foo");
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
        assertStripped(s, "foo foo bar bar");

        s = "foo\n\n    foo\n     bar\nbar";
        assertCompact(s, "foo\nfoo\n bar\nbar");
        assertStripped(s, "foo foo bar bar");
    }

    @Test
    public void textContentBreaks() {
        String s;

        s = "foo\nbar";
        assertCompact(s, "foo\nbar");
        assertStripped(s, "foo bar");

        s = "foo  \nbar";
        assertCompact(s, "foo\nbar");
        assertStripped(s, "foo bar");

        s = "foo\n___\nbar";
        assertCompact(s, "foo\n***\nbar");
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

        html = "foo <foo>foobar</foo> bar";
        assertCompact(html, html);
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
    
    private void assertCompact(String source, String expected) {
        var doc = parse(source);
        var actualRendering = defaultRenderer().render(doc);
        Asserts.assertRendering(source, expected, actualRendering);
    }

    private void assertStripped(String source, String expected) {
        var doc = parse(source);
        var actualRendering = strippedRenderer().render(doc);
        Asserts.assertRendering(source, expected, actualRendering);
    }

    private void assertAll(String source, String expected) {
        assertCompact(source, expected);
        assertStripped(source, expected);
        // TODO
    }
}
