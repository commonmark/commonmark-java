package org.commonmark.internal.util;

public class LinkScanner {

    /**
     * Attempt to scan the contents of a link label (inside the brackets), returning the position after the content or
     * -1. The returned position can either be the closing {@code ]}, or the end of the line if the label continues on
     * the next line.
     */
    public static int scanLinkLabelContent(CharSequence input, int start) {
        for (int i = start; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '\\':
                    if (Parsing.isEscapable(input, i + 1)) {
                        i += 1;
                    }
                    break;
                case ']':
                    return i;
                case '[':
                    // spec: Unescaped square bracket characters are not allowed inside the opening and closing
                    // square brackets of link labels.
                    return -1;
            }
        }
        return input.length();
    }

    /**
     * Attempt to scan a link destination, returning the position after the destination or -1.
     */
    public static int scanLinkDestination(CharSequence input, int start) {
        if (start >= input.length()) {
            return -1;
        }

        if (input.charAt(start) == '<') {
            for (int i = start + 1; i < input.length(); i++) {
                char c = input.charAt(i);
                switch (c) {
                    case '\\':
                        if (Parsing.isEscapable(input, i + 1)) {
                            i += 1;
                        }
                        break;
                    case '\n':
                    case '<':
                        return -1;
                    case '>':
                        return i + 1;
                }
            }
            return -1;
        } else {
            return scanLinkDestinationWithBalancedParens(input, start);
        }
    }

    public static int scanLinkTitle(CharSequence input, int start) {
        if (start >= input.length()) {
            return -1;
        }

        char endDelimiter;
        switch (input.charAt(start)) {
            case '"':
                endDelimiter = '"';
                break;
            case '\'':
                endDelimiter = '\'';
                break;
            case '(':
                endDelimiter = ')';
                break;
            default:
                return -1;
        }

        int afterContent = scanLinkTitleContent(input, start + 1, endDelimiter);
        if (afterContent == -1) {
            return -1;
        }

        if (afterContent >= input.length() || input.charAt(afterContent) != endDelimiter) {
            // missing or wrong end delimiter
            return -1;
        }

        return afterContent + 1;
    }

    public static int scanLinkTitleContent(CharSequence input, int start, char endDelimiter) {
        for (int i = start; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '\\' && Parsing.isEscapable(input, i + 1)) {
                i += 1;
            } else if (c == endDelimiter) {
                return i;
            } else if (endDelimiter == ')' && c == '(') {
                // unescaped '(' in title within parens is invalid
                return -1;
            }
        }
        return input.length();
    }

    // spec: a nonempty sequence of characters that does not start with <, does not include ASCII space or control
    // characters, and includes parentheses only if (a) they are backslash-escaped or (b) they are part of a balanced
    // pair of unescaped parentheses
    private static int scanLinkDestinationWithBalancedParens(CharSequence input, int start) {
        int parens = 0;
        for (int i = start; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '\0':
                case ' ':
                    return i != start ? i : -1;
                case '\\':
                    if (Parsing.isEscapable(input, i + 1)) {
                        i += 1;
                    }
                    break;
                case '(':
                    parens++;
                    // Limit to 32 nested parens for pathological cases
                    if (parens > 32) {
                        return -1;
                    }
                    break;
                case ')':
                    if (parens == 0) {
                        return i;
                    } else {
                        parens--;
                    }
                    break;
                default:
                    // or control character
                    if (Character.isISOControl(c)) {
                        return i != start ? i : -1;
                    }
                    break;
            }
        }
        return input.length();
    }
}
