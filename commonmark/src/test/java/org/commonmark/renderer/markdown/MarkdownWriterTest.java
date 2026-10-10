package org.commonmark.renderer.markdown;

import static org.assertj.core.api.Assertions.assertThat;

import org.commonmark.text.CharMatcher;
import org.junit.jupiter.api.Test;

public class MarkdownWriterTest {

    private static final CharMatcher STAR = c -> c == '*';
    private static final CharMatcher NEWLINE = c -> c == '\n';
    private static final CharMatcher PIPE = c -> c == '|';

    @Test
    public void testTextEscaping() {
        assertThat(text("abc", STAR)).isEqualTo("abc");
        assertThat(text("*abc", STAR)).isEqualTo("\\*abc");
        assertThat(text("abc*", STAR)).isEqualTo("abc\\*");
        assertThat(text("a*b*c", STAR)).isEqualTo("a\\*b\\*c");
        assertThat(text("a**b", STAR)).isEqualTo("a\\*\\*b");
        assertThat(text("***", STAR)).isEqualTo("\\*\\*\\*");
        assertThat(text("*", STAR)).isEqualTo("\\*");
    }

    @Test
    public void testTextEscapingNewline() {
        assertThat(text("a\nb\nc", NEWLINE)).isEqualTo("a&#10;b&#10;c");
        assertThat(text("\n", NEWLINE)).isEqualTo("&#10;");
    }

    @Test
    public void testRawEscaping() {
        StringBuilder sb = new StringBuilder();
        MarkdownWriter writer = new MarkdownWriter(sb);
        writer.pushRawEscape(PIPE);
        writer.raw("a|b|c");
        writer.popRawEscape();
        writer.raw("d|e");
        assertThat(sb.toString()).isEqualTo("a\\|b\\|cd|e");
    }

    @Test
    public void testLastCharAndLineStart() {
        StringBuilder sb = new StringBuilder();
        MarkdownWriter writer = new MarkdownWriter(sb);
        writer.text("a*", STAR);
        assertThat(writer.getLastChar()).isEqualTo('*');
        assertThat(writer.isAtLineStart()).isFalse();
        writer.line();
        assertThat(writer.isAtLineStart()).isTrue();
    }

    private static String text(String s, CharMatcher escape) {
        StringBuilder sb = new StringBuilder();
        MarkdownWriter writer = new MarkdownWriter(sb);
        writer.text(s, escape);
        return sb.toString();
    }
}
