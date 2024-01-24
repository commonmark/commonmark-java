package org.commonmark.renderer.markdown;

import org.commonmark.internal.util.CharMatcher;

import java.io.IOException;
import java.util.LinkedList;

public class MarkdownWriter {

    private final Appendable buffer;

    private int blockSeparator = 0;
    private boolean tight;
    private char lastChar;
    private boolean atLineStart = true;
    private final LinkedList<String> prefixes = new LinkedList<>();

    public MarkdownWriter(Appendable out) {
        buffer = out;
    }

    public char getLastChar() {
        return lastChar;
    }

    /**
     * @return whether we're at the line start (not counting any prefixes), i.e. after a {@link #line} or {@link #block}.
     */
    public boolean isAtLineStart() {
        return atLineStart;
    }

    public void write(String s) {
        flushBlockSeparator();
        append(s);
    }

    public void write(char c) {
        flushBlockSeparator();
        append(c);
    }

    public void writeEscaped(String s, CharMatcher escape) {
        if (s.isEmpty()) {
            return;
        }
        flushBlockSeparator();
        try {
            for (int i = 0; i < s.length(); i++) {
                char ch = s.charAt(i);
                if (ch == '\n') {
                    // Can't escape this with \, use numeric character reference
                    buffer.append("&#10;");
                } else {
                    if (ch == '\\' || escape.matches(ch)) {
                        buffer.append('\\');
                    }
                    buffer.append(ch);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        lastChar = s.charAt(s.length() - 1);
        atLineStart = false;
    }

    public void line() {
        append('\n');
        writePrefixes();
        atLineStart = true;
    }

    /**
     * Enqueue a block separator to be written before the next text is written. Block separators are not written
     * straight away because if there are no more blocks to write we don't want a separator (at the end of the document).
     */
    public void block() {
        // Remember whether this should be a tight or loose separator now because tight could get changed in between
        // this and the next flush.
        blockSeparator = tight ? 1 : 2;
        atLineStart = true;
    }

    public void pushPrefix(String prefix) {
        prefixes.addLast(prefix);
    }

    public void writePrefix(String prefix) {
        boolean tmp = atLineStart;
        write(prefix);
        atLineStart = tmp;
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
        atLineStart = false;
    }

    private void append(char c) {
        try {
            buffer.append(c);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        lastChar = c;
        atLineStart = false;
    }

    private void writePrefixes() {
        if (!prefixes.isEmpty()) {
            for (String prefix : prefixes) {
                append(prefix);
            }
        }
    }

    /**
     * If a block separator has been enqueued with {@link #block()} but not yet written, write it now.
     */
    private void flushBlockSeparator() {
        if (blockSeparator != 0) {
            append('\n');
            writePrefixes();
            if (blockSeparator > 1) {
                append('\n');
                writePrefixes();
            }
            blockSeparator = 0;
        }
    }

    /**
     * @return whether blocks are currently set to tight or loose, see {@link #setTight(boolean)}
     */
    public boolean getTight() {
        return tight;
    }

    /**
     * Change whether blocks are tight or loose. Loose is the default where blocks are separated by a blank line. Tight
     * is where blocks are not separated by a blank line. Tight blocks are used in lists, if there are no blank lines
     * within the list.
     * <p>
     * Note that changing this does not affect block separators that have already been enqueued (with {@link #block()},
     * only future ones.
     */
    public void setTight(boolean tight) {
        this.tight = tight;
    }
}
