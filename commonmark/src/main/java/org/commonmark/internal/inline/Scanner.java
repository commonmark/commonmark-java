package org.commonmark.internal.inline;

import org.commonmark.internal.util.CharMatcher;

import java.util.List;

public class Scanner {

    // Lines without newlines at the end. The scanner will yield `\n` between lines because they're significant for
    // parsing and the final output. There is no `\n` after the last line.
    private final List<CharSequence> lines;
    // Which line we're at.
    private int lineIndex;
    // The index within the line. If index == length(), we pretend that there's a `\n` and only advance after we yield
    // that.
    private int index;

    // Current line or "" if at the end of the lines (using "" instead of null saves a null check)
    private CharSequence line = "";
    private int lineLength = 0;

    // TODO: Visibility
    public Scanner(List<CharSequence> lines, int lineIndex, int index) {
        this.lines = lines;
        this.lineIndex = lineIndex;
        this.index = index;
        if (!lines.isEmpty()) {
            line = lines.get(lineIndex);
            lineLength = line.length();
        }
    }

    public char peek() {
        if (index < lineLength) {
            return line.charAt(index);
        } else {
            if (lineIndex < lines.size() - 1) {
                return '\n';
            } else {
                // Don't return newline for end of last line
                return '\0';
            }
        }
    }

    public char peekPrevious() {
        if (index > 0) {
            int prev = index - 1;
            return line.charAt(prev);
        } else {
            if (lineIndex > 0) {
                return '\n';
            } else {
                return '\0';
            }
        }
    }

    public boolean hasNext() {
        if (index < lineLength) {
            return true;
        } else {
            // No newline at end of last line
            return lineIndex < lines.size() - 1;
        }
    }

    public void next() {
        index++;
        if (index > lineLength) {
            lineIndex++;
            if (lineIndex < lines.size()) {
                line = lines.get(lineIndex);
                lineLength = line.length();
            } else {
                line = "";
                lineLength = 0;
            }
            index = 0;
        }
    }

    public boolean next(char c) {
        if (peek() == c) {
            next();
            return true;
        } else {
            return false;
        }
    }

    public int matchMultiple(char c) {
        int count = 0;
        while (peek() == c) {
            count++;
            next();
        }
        return count;
    }

    public int match(CharMatcher matcher) {
        int count = 0;
        while (matcher.matches(peek())) {
            count++;
            next();
        }
        return count;
    }

    public int whitespace() {
        int count = 0;
        while (true) {
            switch (peek()) {
                case ' ':
                case '\t':
                case '\n':
                case '\u000B':
                case '\f':
                case '\r':
                    count++;
                    next();
                    break;
                default:
                    return count;
            }
        }
    }

    public int find(char c) {
        int count = 0;
        while (true) {
            char cur = peek();
            if (cur == '\0') {
                return -1;
            } else if (cur == c) {
                return count;
            }
            count++;
            next();
        }
    }

    public int find(CharMatcher matcher) {
        int count = 0;
        while (true) {
            char c = peek();
            if (c == '\0') {
                return -1;
            } else if (matcher.matches(c)) {
                return count;
            }
            count++;
            next();
        }
    }

    // Don't expose the int index, because it would be good if we could switch input to a List<String> of lines later
    // instead of one contiguous String.
    public Position position() {
        return new Position(lineIndex, index);
    }

    // For cases where the caller appends the result to a StringBuilder, we could offer another method to avoid some
    // unnecessary copying.
    public CharSequence textBetween(Position begin, Position end) {
        if (begin.lineIndex == end.lineIndex) {
            // Shortcut for common case of text from a single line
            return lines.get(begin.lineIndex).subSequence(begin.index, end.index);
        } else {
            StringBuilder sb = new StringBuilder();

            CharSequence firstLine = lines.get(begin.lineIndex);
            sb.append(firstLine.subSequence(begin.index, firstLine.length()));
            sb.append('\n');

            // Lines between begin and end (we are appending the full line)
            for (int line = begin.lineIndex + 1; line < end.lineIndex; line++) {
                sb.append(lines.get(line));
                sb.append('\n');
            }

            CharSequence lastLine = lines.get(end.lineIndex);
            sb.append(lastLine.subSequence(0, end.index));
            return sb.toString();
        }
    }
}
