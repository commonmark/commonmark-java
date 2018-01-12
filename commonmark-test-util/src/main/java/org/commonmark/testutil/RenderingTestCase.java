package org.commonmark.testutil;

import static org.junit.Assert.assertEquals;

public abstract class RenderingTestCase {

    protected abstract String render(String source);

    protected void assertRendering(String source, String expectedHtml) {
        String html = render(source);

        // include source for better assertion errors
        String expected = showTabs(expectedHtml + "\n\n" + source);
        String actual = showTabs(html + "\n\n" + source);
        assertEquals(expected, actual);
    }

    private static String showTabs(String s) {
        // Tabs are shown as "rightwards arrow" for easier comparison
        return s.replace("\t", "\u2192");
    }
}
