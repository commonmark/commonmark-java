package org.commonmark.internal.inline;

import org.commonmark.internal.util.CharMatcher;
import org.commonmark.internal.util.Parsing;

public class Scanner {

    private final String input;
    private int index;

    // TODO: Visibility
    public Scanner(String input, int index) {
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

    public void skip() {
        index++;
    }

    public boolean skipOne(char c) {
        if (peek() == c) {
            skip();
            return true;
        } else {
            return false;
        }
    }

    public int skip(char c) {
        int count = 0;
        while (peek() == c) {
            count++;
            skip();
        }
        return count;
    }

    public int skip(CharMatcher matcher) {
        int count = 0;
        while (matcher.matches(peek())) {
            count++;
            skip();
        }
        return count;
    }

    public int skipWhitespace() {
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
            skip();
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
            skip();
        }
    }

    // Don't expose the int index, because it would be good if we could switch input to a List<String> of lines later
    // instead of one contiguous String.
    public Position position() {
        return new Position(index);
    }

    public String textBetween(Position begin, Position end) {
        return input.substring(begin.index, end.index);
    }
}
