package org.commonmark.internal.inline;

public class Scanner {

    private final String input;
    private int index;
    private int consumed = 0;

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
        consumed++;
    }

    public int consumed() {
        return consumed;
    }
}
