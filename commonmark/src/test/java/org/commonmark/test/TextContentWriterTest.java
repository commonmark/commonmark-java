package org.commonmark.test;

import org.commonmark.renderer.text.TextContentWriter;
import org.junit.Test;

import static org.junit.Assert.*;

public class TextContentWriterTest {

    @Test
    public void whitespace() throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        TextContentWriter writer = new TextContentWriter(stringBuilder);
        writer.write("foo");
        writer.whitespace();
        writer.write("bar");
        assertEquals("foo bar", stringBuilder.toString());
    }

    @Test
    public void colon() throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        TextContentWriter writer = new TextContentWriter(stringBuilder);
        writer.write("foo");
        writer.colon();
        writer.write("bar");
        assertEquals("foo:bar", stringBuilder.toString());
    }

    @Test
    public void line() throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        TextContentWriter writer = new TextContentWriter(stringBuilder);
        writer.write("foo");
        writer.line();
        writer.write("bar");
        assertEquals("foo\nbar", stringBuilder.toString());
    }

    @Test
    public void writeStripped() throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        TextContentWriter writer = new TextContentWriter(stringBuilder);
        writer.writeStripped("foo\n bar");
        assertEquals("foo bar", stringBuilder.toString());
    }

    @Test
    public void write() throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        TextContentWriter writer = new TextContentWriter(stringBuilder);
        writer.writeStripped("foo bar");
        assertEquals("foo bar", stringBuilder.toString());
    }

    @Test
    public void append() throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        SubclassedWriter writer = new SubclassedWriter(stringBuilder);
        writer.write("foo");
        assertEquals("foo", stringBuilder.toString());
    }

    @Test
    public void getLastChar() throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        SubclassedWriter writer = new SubclassedWriter(stringBuilder);
        writer.write("foo");
        assertEquals('o', writer.getLastChar());
    }

    @Test
    public void getBuffer() throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        SubclassedWriter writer = new SubclassedWriter(stringBuilder);
        writer.write("foo");
        assertEquals("foo", writer.getBuffer().toString());
    }

    private static class SubclassedWriter extends TextContentWriter {

        SubclassedWriter(StringBuilder out) {
            super(out);
        }

        @Override
        protected Appendable getBuffer() {
            return super.getBuffer();
        }

        @Override
        protected char getLastChar() {
            return super.getLastChar();
        }

    }
}
