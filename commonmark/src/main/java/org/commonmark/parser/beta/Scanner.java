package org.commonmark.parser.beta;

import java.util.List;
import org.commonmark.node.SourceSpan;
import org.commonmark.parser.SourceLine;
import org.commonmark.parser.SourceLines;
import org.commonmark.text.CharMatcher;

/**
 * A cursor over source lines for parsing inline content.
 *
 * <p>Lines are scanned as one sequence of characters with a {@code \n} yielded between lines but
 * not after the last line. As usual for Java strings, positions and the counts returned by the
 * methods are in {@code char}s.
 */
public class Scanner {

    /**
     * Character representing the end of input source (or outside of the text in case of the
     * "previous" methods).
     *
     * <p>Note that we can use NULL to represent this because CommonMark does not allow those in the
     * input (we replace them in the beginning of parsing). It is not a searchable source character:
     * {@link #find(char)} and {@link #findPrev(char, Position)} skip it instead of matching it.
     */
    public static final char END = '\0';

    // Lines without newlines at the end. The scanner will yield `\n` between lines because they're
    // significant for parsing and the final output. There is no `\n` after the last line.
    private final List<SourceLine> lines;
    // Which line we're at.
    private int lineIndex;
    // The index within the line. If index == length(), we pretend that there's a `\n` and only
    // advance after we yield that.
    private int index;

    // Current line or "" if at the end of the lines (using "" instead of null saves a null check)
    private SourceLine line = SourceLine.of("", null);
    private int lineLength = 0;

    Scanner(List<SourceLine> lines, int lineIndex, int index) {
        this.lines = lines;
        this.lineIndex = lineIndex;
        this.index = index;
        if (!lines.isEmpty()) {
            checkPosition(lineIndex, index);
            setLine(lines.get(lineIndex));
        }
    }

    /**
     * Create a scanner positioned at the beginning of {@code lines}.
     *
     * @param lines the source lines to scan
     * @return a new scanner
     */
    public static Scanner of(SourceLines lines) {
        return new Scanner(lines.getLines(), 0, 0);
    }

    /**
     * Return the character at the current position without advancing.
     *
     * @return the current character, {@code \n} at the end of a line that is not the last one, or
     *     {@link #END} at the end of input
     */
    public char peek() {
        if (index < lineLength) {
            return line.getContent().charAt(index);
        } else {
            if (lineIndex < lines.size() - 1) {
                return '\n';
            } else {
                // Don't return newline for end of last line
                return END;
            }
        }
    }

    /**
     * Return the code point at the current position without advancing. If the current character is
     * a high surrogate followed by a low surrogate on the same line, the pair is combined.
     *
     * @return the current code point, {@code \n} at the end of a line that is not the last one, or
     *     {@link #END} at the end of input
     */
    public int peekCodePoint() {
        if (index < lineLength) {
            char c = line.getContent().charAt(index);
            if (Character.isHighSurrogate(c) && index + 1 < lineLength) {
                char low = line.getContent().charAt(index + 1);
                if (Character.isLowSurrogate(low)) {
                    return Character.toCodePoint(c, low);
                }
            }
            return c;
        } else {
            if (lineIndex < lines.size() - 1) {
                return '\n';
            } else {
                // Don't return newline for end of last line
                return END;
            }
        }
    }

    /**
     * Return the code point before the current position without moving. If the previous character
     * is a low surrogate preceded by a high surrogate on the same line, the pair is combined.
     *
     * @return the previous code point, {@code \n} at the beginning of a line that is not the first
     *     one, or {@link #END} at the beginning of input
     */
    public int peekPreviousCodePoint() {
        if (index > 0) {
            int prev = index - 1;
            char c = line.getContent().charAt(prev);
            if (Character.isLowSurrogate(c) && prev > 0) {
                char high = line.getContent().charAt(prev - 1);
                if (Character.isHighSurrogate(high)) {
                    return Character.toCodePoint(high, c);
                }
            }
            return c;
        } else {
            if (lineIndex > 0) {
                return '\n';
            } else {
                return END;
            }
        }
    }

    /**
     * @return true if there is a character at the current position (including a {@code \n} between
     *     lines), false at the end of input
     */
    public boolean hasNext() {
        if (index < lineLength) {
            return true;
        } else {
            // No newline at end of last line
            return lineIndex < lines.size() - 1;
        }
    }

    /** Advance by one character, moving to the next line after the {@code \n} between lines. */
    public void next() {
        index++;
        if (index > lineLength) {
            lineIndex++;
            if (lineIndex < lines.size()) {
                setLine(lines.get(lineIndex));
            } else {
                setLine(SourceLine.of("", null));
            }
            index = 0;
        }
    }

    // Move back by one character, reversing next(). From index zero the position moves to the
    // previous line's length (the newline between lines). At the beginning of the input, including
    // an empty input, the position is left unchanged.
    private void prev() {
        if (index > 0) {
            index--;
        } else if (lineIndex > 0) {
            lineIndex--;
            if (lineIndex < lines.size()) {
                setLine(lines.get(lineIndex));
            } else {
                // Landed past the end (after next() at EOF); same representation as next().
                setLine(SourceLine.of("", null));
            }
            index = lineLength;
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
     * Check if we have the specified content on the line and advanced the position. Note that if
     * you want to match newline characters, use {@link #next(char)}.
     *
     * @param content the text content to match on a single line (excluding newline characters)
     * @return true if matched and position was advanced, false otherwise
     */
    public boolean next(String content) {
        if (index < lineLength && index + content.length() <= lineLength) {
            // Can't use startsWith because it's not available on CharSequence
            for (int i = 0; i < content.length(); i++) {
                if (line.getContent().charAt(index + i) != content.charAt(i)) {
                    return false;
                }
            }
            index += content.length();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Advance past consecutive occurrences of {@code c}.
     *
     * @param c the character to match (including a newline)
     * @return the number of characters matched, 0 if the current character is not {@code c}
     */
    public int matchMultiple(char c) {
        int count = 0;
        while (peek() == c) {
            count++;
            next();
        }
        return count;
    }

    /**
     * Advance past consecutive characters matched by {@code matcher}.
     *
     * @param matcher matcher applied to each character, including {@code \n} between lines and
     *     {@link #END} at the end of input
     * @return the number of characters matched
     */
    public int match(CharMatcher matcher) {
        int count = 0;
        while (matcher.matches(peek())) {
            count++;
            next();
        }
        return count;
    }

    /**
     * Advance past consecutive whitespace characters: space, tab, newline, line tabulation, form
     * feed and carriage return.
     *
     * @return the number of characters skipped
     */
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

    /**
     * Find the next occurrence of {@code c} at or after the current position.
     *
     * <p>If found, the scanner is left at the match. Otherwise it is left at the end of input and
     * {@code -1} is returned. {@link #END} is never matched.
     *
     * @param c character to find (including a newline)
     * @return the number of characters moved forward, or {@code -1} if not found
     */
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

    /**
     * Find the next character matched by {@code matcher} at or after the current position.
     *
     * <p>See {@link #find(char)} for where the scanner is left. {@link #END} is not passed to
     * {@code matcher}.
     *
     * @param matcher matcher applied to each character
     * @return the number of characters moved forward, or {@code -1} if not found
     */
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

    /**
     * Find the nearest occurrence of {@code c} at or before the current position and at or after
     * {@code bound}.
     *
     * <p>The current character is included, matching {@link #find(char)}, so an immediate match
     * returns {@code 0}. From the end of input a match on the character immediately before the end
     * returns {@code 1}. A newline between lines can be matched. {@link #END} is skipped and never
     * matches.
     *
     * <p>{@code bound} is inclusive: the character there is examined, and the search never moves
     * before it. If {@code bound} is the current position, only that character is checked. If there
     * is no match, the scanner is left at {@code bound} and {@code -1} is returned. On an empty
     * input the search returns {@code -1}; the position at the beginning of that input is a valid
     * bound.
     *
     * <p>The bound is checked before the scanner moves. It must have valid coordinates for this
     * scanner and must not be after the current position. As with {@link #setPosition(Position)},
     * positions are expected to come from the same scanner.
     *
     * @param c character to find (including a newline)
     * @param bound inclusive lower bound
     * @return the number of characters moved backward, or {@code -1} if not found
     * @throws IllegalArgumentException if {@code bound} is after the current position or outside
     *     the input
     */
    public int findPrev(char c, Position bound) {
        checkLowerBound(bound);
        return findPrev(c, null, bound.lineIndex, bound.index);
    }

    /**
     * Find the nearest character matched by {@code matcher} at or before the current position and
     * at or after {@code bound}.
     *
     * <p>See {@link #findPrev(char, Position)} for the bound, {@link #END}, and a search with no
     * match. {@link #END} is not passed to {@code matcher}.
     *
     * @param matcher matcher applied to each character
     * @param bound inclusive lower bound
     * @return the number of characters moved backward, or {@code -1} if not found
     * @throws IllegalArgumentException if {@code bound} is after the current position or outside
     *     the input
     */
    public int findPrev(CharMatcher matcher, Position bound) {
        checkLowerBound(bound);
        return findPrev(END, matcher, bound.lineIndex, bound.index);
    }

    // matcher == null matches c exactly, so the char overload allocates nothing per character.
    private int findPrev(char c, CharMatcher matcher, int boundLine, int boundIndex) {
        int count = 0;
        while (true) {
            char cur = peek();
            if (cur != END && (matcher != null ? matcher.matches(cur) : cur == c)) {
                return count;
            }
            if (lineIndex == boundLine && index == boundIndex) {
                return -1;
            }
            int lineBefore = lineIndex;
            int indexBefore = index;
            prev();
            // Beginning of input: prev() is a no-op, so stop instead of spinning.
            if (lineIndex == lineBefore && index == indexBefore) {
                return -1;
            }
            count++;
        }
    }

    // Don't expose the int index, because it would be good if we could switch input to a
    // List<String> of lines later instead of one contiguous String.
    /**
     * @return the current position, for use with {@link #setPosition(Position)}, {@link
     *     #getSource(Position, Position)} and {@link #findPrev(char, Position)}
     */
    public Position position() {
        return new Position(lineIndex, index);
    }

    /**
     * Move to a position previously returned by {@link #position()} on this scanner.
     *
     * @param position the position to move to
     * @throws IllegalArgumentException if {@code position} is outside the input
     */
    public void setPosition(Position position) {
        checkPosition(position.lineIndex, position.index);
        this.lineIndex = position.lineIndex;
        this.index = position.index;
        setLine(lines.get(this.lineIndex));
    }

    // For cases where the caller appends the result to a StringBuilder, we could offer another
    // method to avoid some unnecessary copying.
    /**
     * Get the source between two positions of this scanner, with {@code begin} inclusive and {@code
     * end} exclusive. A {@code \n} between lines is represented by splitting into separate lines.
     *
     * @param begin the start position
     * @param end the end position, not before {@code begin}
     * @return the source lines, including their source spans if available
     */
    public SourceLines getSource(Position begin, Position end) {
        if (begin.lineIndex == end.lineIndex) {
            // Shortcut for common case of text from a single line
            SourceLine line = lines.get(begin.lineIndex);
            CharSequence newContent = line.getContent().subSequence(begin.index, end.index);
            SourceSpan newSourceSpan = null;
            SourceSpan sourceSpan = line.getSourceSpan();
            if (sourceSpan != null) {
                newSourceSpan = sourceSpan.subSpan(begin.index, end.index);
            }
            return SourceLines.of(SourceLine.of(newContent, newSourceSpan));
        } else {
            SourceLines sourceLines = SourceLines.empty();

            SourceLine firstLine = lines.get(begin.lineIndex);
            sourceLines.addLine(firstLine.substring(begin.index, firstLine.getContent().length()));

            // Lines between begin and end (we are appending the full line)
            for (int line = begin.lineIndex + 1; line < end.lineIndex; line++) {
                sourceLines.addLine(lines.get(line));
            }

            SourceLine lastLine = lines.get(end.lineIndex);
            sourceLines.addLine(lastLine.substring(0, end.index));
            return sourceLines;
        }
    }

    private void setLine(SourceLine line) {
        this.line = line;
        this.lineLength = line.getContent().length();
    }

    private void checkPosition(int lineIndex, int index) {
        if (lineIndex < 0 || lineIndex >= lines.size()) {
            throw new IllegalArgumentException(
                    "Line index " + lineIndex + " out of range, number of lines: " + lines.size());
        }
        SourceLine line = lines.get(lineIndex);
        if (index < 0 || index > line.getContent().length()) {
            throw new IllegalArgumentException(
                    "Index " + index + " out of range, line length: " + line.getContent().length());
        }
    }

    private void checkLowerBound(Position position) {
        // Empty input has no lines; its only position is the canonical (0, 0) cursor.
        if (lines.isEmpty()) {
            if (position.lineIndex != 0) {
                throw new IllegalArgumentException(
                        "Line index "
                                + position.lineIndex
                                + " out of range, number of lines: "
                                + lines.size());
            }
            if (position.index != 0) {
                throw new IllegalArgumentException(
                        "Index " + position.index + " out of range, line length: 0");
            }
        } else {
            checkPosition(position.lineIndex, position.index);
        }
        if (position.lineIndex > lineIndex
                || (position.lineIndex == lineIndex && position.index > index)) {
            throw new IllegalArgumentException(
                    "Bound line "
                            + position.lineIndex
                            + " index "
                            + position.index
                            + " is after the current position line "
                            + lineIndex
                            + " index "
                            + index);
        }
    }
}
