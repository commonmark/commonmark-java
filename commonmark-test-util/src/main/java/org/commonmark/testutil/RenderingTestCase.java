package org.commonmark.testutil;

import static org.junit.Assert.assertEquals;

public abstract class RenderingTestCase {

    protected abstract String render(String source);

    protected void assertRendering(String source, String expectedResult) {
        String renderedContent = render(source);

        // include source for better assertion errors
        String expected = showTabs(expectedResult + "\n\n" + source);
        String actual = showTabs(renderedContent + "\n\n" + source);
        assertEquals(expected, actual);
    }

    private static String showTabs(String s) {
        // Tabs are shown as "rightwards arrow" for easier comparison
        return s.replace("\t", "\u2192");
    }
}
