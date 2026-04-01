package org.commonmark.test;

import org.commonmark.renderer.text.TextContentWriter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TextContentWriterTest {

    @Test
    public void whitespace() {
        StringBuilder stringBuilder = new StringBuilder();
        TextContentWriter writer = new TextContentWriter(stringBuilder);
        writer.write("foo");
        writer.whitespace();
        writer.write("bar");
        assertThat(stringBuilder.toString()).isEqualTo("foo bar");
    }

    @Test
    public void colon() {
        StringBuilder stringBuilder = new StringBuilder();
        TextContentWriter writer = new TextContentWriter(stringBuilder);
        writer.write("foo");
        writer.colon();
        writer.write("bar");
        assertThat(stringBuilder.toString()).isEqualTo("foo:bar");
    }

    @Test
    public void line() {
        StringBuilder stringBuilder = new StringBuilder();
        TextContentWriter writer = new TextContentWriter(stringBuilder);
        writer.write("foo");
        writer.line();
        writer.write("bar");
        assertThat(stringBuilder.toString()).isEqualTo("foo\nbar");
    }

    @Test
    public void writeStripped() {
        StringBuilder stringBuilder = new StringBuilder();
        TextContentWriter writer = new TextContentWriter(stringBuilder);
        writer.writeStripped("foo\n bar");
        assertThat(stringBuilder.toString()).isEqualTo("foo bar");
    }

    @Test
    public void write() {
        StringBuilder stringBuilder = new StringBuilder();
        TextContentWriter writer = new TextContentWriter(stringBuilder);
        writer.writeStripped("foo bar");
        assertThat(stringBuilder.toString()).isEqualTo("foo bar");
    }
}
