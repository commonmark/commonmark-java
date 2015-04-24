package org.commonmark.internal.util;

public class Parsing {

    private static final String[] TAB_SPACES = new String[]{"    ", "   ", "  ", " "};

    public static boolean isBlank(CharSequence s) {
        return findNonSpace(s, 0) == -1;
    }

    public static int findNonSpace(CharSequence s, int startIndex) {
        for (int i = startIndex; i < s.length(); i++) {
            switch (s.charAt(i)) {
                case ' ':
                    break;
                case '\n':
                    break;
                default:
                    return i;
            }
        }
        return -1;
    }

    public static int findLineBreak(CharSequence s, int startIndex) {
        for (int i = startIndex; i < s.length(); i++) {
            switch (s.charAt(i)) {
                case '\n':
                case '\r':
                    return i;
            }
        }
        return -1;
    }

    public static boolean isLetter(CharSequence s, int index) {
        int codePoint = Character.codePointAt(s, index);
        return Character.isLetter(codePoint);
    }

    /**
     * Prepares the input line, replacing {@code \0} and converting tabs to spaces using a 4-space tab stop.
     */
    public static CharSequence prepareLine(CharSequence line) {
        // Avoid building a new string in the majority of cases (no \t or \0)
        StringBuilder sb = null;
        int tabStopStart = 0;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            switch (line.charAt(i)) {
                case '\t':
                    if (sb == null) {
                        sb = new StringBuilder(line.length());
                        sb.append(line, 0, i);
                    }
                    int indexInTab = (i - tabStopStart) % 4;
                    sb.append(TAB_SPACES[indexInTab]);
                    tabStopStart = i + 1;
                    break;
                case '\0':
                    if (sb == null) {
                        sb = new StringBuilder(line.length());
                        sb.append(line, 0, i);
                    }
                    sb.append('\uFFFD');
                    break;
                default:
                    if (sb != null) {
                        sb.append(c);
                    }
            }
        }

        if (sb != null) {
            return sb.toString();
        } else {
            return line;
        }
    }
}
