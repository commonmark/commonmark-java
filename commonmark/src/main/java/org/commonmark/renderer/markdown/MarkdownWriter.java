package org.commonmark.renderer.markdown;

import org.commonmark.internal.util.CharMatcher;

import java.io.IOException;
import java.util.LinkedList;

public class MarkdownWriter {

    private final Appendable buffer;

    private boolean finishBlock = false;
    private boolean tight;
    private char lastChar;
    private final LinkedList<String> prefixes = new LinkedList<>();

    public MarkdownWriter(Appendable out) {
        buffer = out;
    }

    public char getLastChar() {
        return lastChar;
    }

    public void block() {
        finishBlock = true;
    }

    public void line() {
        append('\n');
        writePrefixes();
    }

    public void write(String s) {
        finishBlockIfNeeded();
        append(s);
    }

    public void write(char c) {
        finishBlockIfNeeded();
        append(c);
    }

    public void writeEscaped(String s, CharMatcher escape) {
        if (s.isEmpty()) {
            return;
        }
        finishBlockIfNeeded();
        try {
            for (int i = 0; i < s.length(); i++) {
                char ch = s.charAt(i);
                if (ch == '\\' || escape.matches(ch)) {
                    buffer.append('\\');
                }
                buffer.append(ch);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        lastChar = s.charAt(s.length() - 1);
    }

    public void pushPrefix(String prefix) {
        prefixes.addLast(prefix);
    }

    public void popPrefix() {
        prefixes.removeLast();
    }

    private void append(String s) {
        try {
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
            buffer.append(c);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        lastChar = c;
    }

    private void finishBlockIfNeeded() {
        if (finishBlock) {
            finishBlock = false;
            append('\n');
            writePrefixes();
            if (!tight) {
                append('\n');
                writePrefixes();
            }
        }
    }

    private void writePrefixes() {
        if (!prefixes.isEmpty()) {
            for (String prefix : prefixes) {
                append(prefix);
            }
        }
    }

    public boolean getTight() {
        return tight;
    }

    public void setTight(boolean tight) {
        this.tight = tight;
    }
}
