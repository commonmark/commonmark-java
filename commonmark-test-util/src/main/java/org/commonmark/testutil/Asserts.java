package org.commonmark.testutil;

import static org.junit.Assert.assertEquals;

public class Asserts {
    public static void assertRendering(String source, String expectedRendering, String actualRendering) {
        // include source for better assertion errors
        String expected = showTabs(expectedRendering + "\n\n" + source);
        String actual = showTabs(actualRendering + "\n\n" + source);
        assertEquals(expected, actual);
    }

    private static String showTabs(String s) {
        // Tabs are shown as "rightwards arrow" for easier comparison
        return s.replace("\t", "\u2192");
    }
}
