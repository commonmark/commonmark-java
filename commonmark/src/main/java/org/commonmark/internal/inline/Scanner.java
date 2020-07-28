package org.commonmark.internal.inline;

import org.commonmark.internal.util.CharMatcher;
import org.commonmark.internal.util.Parsing;

public class Scanner {

    private final CharSequence input;
    private int index;

    // TODO: Visibility
    public Scanner(CharSequence input, int index) {
        this.input = input;
        this.index = index;
    }

    public char peek() {
        if (index >= input.length()) {
            return '\0';
        } else {
            return input.charAt(index);
        }
    }

    public char peekPrevious() {
        int prev = index - 1;
        if (prev >= 0 && prev < input.length()) {
            return input.charAt(prev);
        } else {
            return '\0';
        }
    }

    public boolean hasNext() {
        return index < input.length();
    }

    public void next() {
        index++;
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
        int newIndex = Parsing.skipWhitespace(input, index, input.length());
        int count = newIndex - index;
        index = newIndex;
        return count;
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
        return new Position(index);
    }

    // For cases where the caller appends the result to a StringBuilder, we could offer another method to avoid some
    // unnecessary copying.
    public CharSequence textBetween(Position begin, Position end) {
        return input.subSequence(begin.index, end.index);
    }
}
