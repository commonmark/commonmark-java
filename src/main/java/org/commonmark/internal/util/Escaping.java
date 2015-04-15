package org.commonmark.internal.util;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Escaping {

    public static final String ESCAPABLE = "[!\"#$%&\'()*+,./:;<=>?@\\[\\\\\\]^_`{|}~-]";

    private static final String ENTITY = "&(?:#x[a-f0-9]{1,8}|#[0-9]{1,8}|[a-z][a-z0-9]{1,31});";

    private static final Pattern BACKSLASH_OR_AMP = Pattern.compile("[\\\\&]");

    private static final Pattern ENTITY_OR_ESCAPED_CHAR =
            Pattern.compile("\\\\" + ESCAPABLE + '|' + ENTITY, Pattern.CASE_INSENSITIVE);

    private static final String XML_SPECIAL = "[&<>\"]";

    private static final Pattern XML_SPECIAL_RE = Pattern.compile(XML_SPECIAL);

    private static final Pattern XML_SPECIAL_OR_ENTITY =
            Pattern.compile(ENTITY + '|' + XML_SPECIAL, Pattern.CASE_INSENSITIVE);

    // From MDN encodeURI documentation
    private static final Pattern ESCAPE_IN_URI =
            Pattern.compile("[^%;,/?:@&=+$#\\-_.!~*'()a-zA-Z0-9]");

    private static final char[] HEX_DIGITS =
            new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private static final Pattern WHITESPACE = Pattern.compile("[ \t\r\n]+");

    private static final Replacer UNSAFE_CHAR_REPLACER = new Replacer() {
        @Override
        public void replace(String input, StringBuilder sb) {
            switch (input) {
                case "&":
                    sb.append("&amp;");
                    break;
                case "<":
                    sb.append("&lt;");
                    break;
                case ">":
                    sb.append("&gt;");
                    break;
                case "\"":
                    sb.append("&quot;");
                    break;
                default:
                    sb.append(input);
            }
        }
    };

    private static final Replacer UNESCAPE_REPLACER = new Replacer() {
        @Override
        public void replace(String input, StringBuilder sb) {
            if (input.charAt(0) == '\\') {
                sb.append(input, 1, input.length());
            } else {
                sb.append(Html5Entities.entityToString(input));
            }
        }
    };

    private static final Replacer URI_REPLACER = new Replacer() {
        @Override
        public void replace(String input, StringBuilder sb) {
            byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
            for (byte b : bytes) {
                sb.append('%');
                sb.append(HEX_DIGITS[(b >> 4) & 0xF]);
                sb.append(HEX_DIGITS[b & 0xF]);
            }
        }
    };

    public static String escapeHtml(String input, boolean preserveEntities) {
        Pattern p = preserveEntities ? XML_SPECIAL_OR_ENTITY : XML_SPECIAL_RE;
        return replaceAll(p, input, UNSAFE_CHAR_REPLACER);
    }

    /**
     * Replace entities and backslash escapes with literal characters.
     */
    public static String unescapeString(String s) {
        if (BACKSLASH_OR_AMP.matcher(s).find()) {
            return replaceAll(ENTITY_OR_ESCAPED_CHAR, s, UNESCAPE_REPLACER);
        } else {
            return s;
        }
    }

    public static String normalizeURI(String uri) {
        return replaceAll(ESCAPE_IN_URI, uri, URI_REPLACER);
    }

    public static String normalizeReference(String input) {
        // foo: is this the same as JS?
        return WHITESPACE.matcher(input.toLowerCase(Locale.ROOT)).replaceAll(" ");
    }

    private static String replaceAll(Pattern p, String s, Replacer replacer) {
        Matcher matcher = p.matcher(s);

        if (!matcher.find()) {
            return s;
        }

        StringBuilder sb = new StringBuilder(s.length() + 16);
        int lastEnd = 0;
        do {
            sb.append(s, lastEnd, matcher.start());
            replacer.replace(matcher.group(), sb);
            lastEnd = matcher.end();
        } while (matcher.find());

        if (lastEnd != s.length()) {
            sb.append(s, lastEnd, s.length());
        }
        return sb.toString();
    }

    private interface Replacer {
        void replace(String input, StringBuilder sb);
    }
}
