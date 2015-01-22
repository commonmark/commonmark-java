package com.atlassian.rstocker.cm;

import java.util.regex.Pattern;

public class Common {

    private static final String ENTITY = "&(?:#x[a-f0-9]{1,8}|#[0-9]{1,8}|[a-z][a-z0-9]{1,31});";

    // foo: not sure about [ in [
    private static final String ESCAPABLE = "[!\"#$%&\'()*+,./:;<=>?@\\[\\\\\\]^_`{|}~-]";


    // foo: not sure about the backslashes here
    private static final Pattern reBackslashOrAmp = Pattern.compile("[\\\\&]");

    // foo: had flags 'gi' before
    private static final Pattern  reEntityOrEscapedChar = Pattern.compile("\\\\" + ESCAPABLE + '|' + ENTITY, Pattern.CASE_INSENSITIVE);

    static char unescapeChar(String s) {
        if (s.charAt(0) == '\\') {
            return s.charAt(1);
        } else {
            return s.charAt(1); // foo: entityToChar(s);
        }
    }

    // Replace entities and backslash escapes with literal characters.
    public static String unescapeString(String s) {
        // foo:
//        if (reBackslashOrAmp.matcher(s).find()) {
//            return s.replace(reEntityOrEscapedChar, unescapeChar);
//        } else {
            return s;
//        }
    };


}
