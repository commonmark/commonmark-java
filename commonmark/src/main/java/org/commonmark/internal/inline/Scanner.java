package org.commonmark.internal.inline;

import org.commonmark.internal.util.CharMatcher;

import java.util.Collections;
import java.util.List;

public class Scanner {

    /**
     * Character representing the end of input (or outside of the text in case of the "previous" methods).
     * <p>
     * Note that we can use NULL to represent this because CommonMark does not allow those in the input (we replace them
     * in the beginning of parsing).
     */
    public static final char END = '\0';

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

    Scanner(List<CharSequence> lines, int lineIndex, int index) {
        this.lines = lines;
        this.lineIndex = lineIndex;
        this.index = index;
        if (!lines.isEmpty()) {
            checkPosition(lineIndex, index);
            setLine(lines.get(lineIndex));
        }
    }

    public static Scanner of(List<CharSequence> lines) {
        return new Scanner(lines, 0, 0);
    }

    public static Scanner of(CharSequence line) {
        return new Scanner(Collections.singletonList(line), 0, 0);
    }

    public char peek() {
        if (index < lineLength) {
            return line.charAt(index);
        } else {
            if (lineIndex < lines.size() - 1) {
                return '\n';
            } else {
                // Don't return newline for end of last line
                return END;
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
                return END;
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
                setLine(lines.get(lineIndex));
            } else {
                setLine("");
            }
            index = 0;
        }
    }

    /**
     * Check if the specified char is next and advance the position.
     *
     * @param c the char to check (including newline characters)
     * @return true if matched and position was advanced, false otherwise
     */
    public boolean next(char c) {
        if (peek() == c) {
            next();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Check if we have the specified content on the line and advanced the position. Note that if you want to match
     * newline characters, use {@link #next(char)}.
     *
     * @param content the text content to match on a single line (excluding newline characters)
     * @return true if matched and position was advanced, false otherwise
     */
    public boolean next(String content) {
        if (index < lineLength && index + content.length() <= lineLength) {
            // Can't use startsWith because it's not available on CharSequence
            for (int i = 0; i < content.length(); i++) {
                if (line.charAt(index + i) != content.charAt(i)) {
                    return false;
                }
            }
            index += content.length();
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
            if (cur == Scanner.END) {
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
            if (c == END) {
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

    public void setPosition(Position position) {
        checkPosition(position.lineIndex, position.index);
        this.lineIndex = position.lineIndex;
        this.index = position.index;
        setLine(lines.get(this.lineIndex));
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

    private void setLine(CharSequence line) {
        this.line = line;
        this.lineLength = line.length();
    }

    private void checkPosition(int lineIndex, int index) {
        if (lineIndex < 0 || lineIndex >= lines.size()) {
            throw new IllegalArgumentException("Line index " + lineIndex + " out of range, number of lines: " + lines.size());
        }
        CharSequence line = lines.get(lineIndex);
        if (index < 0 || index > line.length()) {
            throw new IllegalArgumentException("Index " + index + " out of range, line length: " + line.length());
        }
    }
}
