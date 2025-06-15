package org.commonmark.testutil;

import static org.assertj.core.api.Assertions.assertThat;

public class Asserts {
    public static void assertRendering(String source, String expectedRendering, String actualRendering) {
        // include source for better assertion errors
        String expected = showTabs(expectedRendering + "\n\n" + source);
        String actual = showTabs(actualRendering + "\n\n" + source);
        assertThat(actual).isEqualTo(expected);
    }

    private static String showTabs(String s) {
        // Tabs are shown as "rightwards arrow" for easier comparison
        return s.replace("\t", "\u2192");
    }
}
