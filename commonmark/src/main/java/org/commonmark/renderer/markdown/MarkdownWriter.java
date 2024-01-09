package org.commonmark.renderer.markdown;

import java.io.IOException;

public class MarkdownWriter {

    private final Appendable buffer;

    private boolean prependLine = false;

    public MarkdownWriter(Appendable out) {
        buffer = out;
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
    }

    private void append(char c) {
        try {
            appendLineIfNeeded();
            buffer.append(c);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void appendLineIfNeeded() throws IOException {
        if (prependLine) {
            buffer.append('\n');
            prependLine = false;
        }
    }
}
