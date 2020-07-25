package org.commonmark.internal.inline;

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

    public int skip(char c) {
        int count = 0;
        while (peek() == c) {
            count++;
            skip();
        }
        return count;
    }

    public boolean find(char c) {
        int newIndex = Parsing.find(c, input, index);
        if (newIndex == -1) {
            return false;
        } else {
            index = newIndex;
            return true;
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
