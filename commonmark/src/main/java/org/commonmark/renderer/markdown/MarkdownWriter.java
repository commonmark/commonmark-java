package org.commonmark.renderer.markdown;

import java.io.IOException;

public class MarkdownWriter {

    private final Appendable buffer;

    private boolean prependLine = false;
    private char lastChar;

    public MarkdownWriter(Appendable out) {
        buffer = out;
    }

    public char getLastChar() {
        return lastChar;
    }

    public void block() {
        append('\n');
        prependLine = true;
    }

    public void write(String s) {
        append(s);
    }

    public void write(char c) {
        append(c);
    }

    private void append(String s) {
        try {
            appendLineIfNeeded();
            buffer.append(s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int length = s.length();
        if (length != 0) {
            lastChar = s.charAt(length - 1);
        }
    }

    private void append(char c) {
        try {
            appendLineIfNeeded();
            buffer.append(c);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        lastChar = c;
    }

    private void appendLineIfNeeded() throws IOException {
        if (prependLine) {
            buffer.append('\n');
            prependLine = false;
        }
    }
}
