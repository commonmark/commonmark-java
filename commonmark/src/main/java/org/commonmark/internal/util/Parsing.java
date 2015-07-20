package org.commonmark.internal.util;

public class Parsing {

    private static final String[] TAB_SPACES = new String[]{"    ", "   ", "  ", " "};

    private static final String TAGNAME = "[A-Za-z][A-Za-z0-9-]*";
    private static final String ATTRIBUTENAME = "[a-zA-Z_:][a-zA-Z0-9:._-]*";
    private static final String UNQUOTEDVALUE = "[^\"'=<>`\\x00-\\x20]+";
    private static final String SINGLEQUOTEDVALUE = "'[^']*'";
    private static final String DOUBLEQUOTEDVALUE = "\"[^\"]*\"";
    private static final String ATTRIBUTEVALUE = "(?:" + UNQUOTEDVALUE + "|" + SINGLEQUOTEDVALUE
            + "|" + DOUBLEQUOTEDVALUE + ")";
    private static final String ATTRIBUTEVALUESPEC = "(?:" + "\\s*=" + "\\s*" + ATTRIBUTEVALUE
            + ")";
    private static final String ATTRIBUTE = "(?:" + "\\s+" + ATTRIBUTENAME + ATTRIBUTEVALUESPEC
            + "?)";

    public static final String OPENTAG = "<" + TAGNAME + ATTRIBUTE + "*" + "\\s*/?>";
    public static final String CLOSETAG = "</" + TAGNAME + "\\s*[>]";

    public static boolean isBlank(CharSequence s) {
        return findNonSpace(s, 0) == -1;
    }

    public static int findNonSpace(CharSequence s, int startIndex) {
        for (int i = startIndex; i < s.length(); i++) {
            switch (s.charAt(i)) {
                case ' ':
                case '\t':
                case '\n':
                case '\u000B':
                case '\f':
                case '\r':
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
