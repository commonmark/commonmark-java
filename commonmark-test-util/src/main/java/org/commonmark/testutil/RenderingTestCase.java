package org.commonmark.testutil;

public abstract class RenderingTestCase {

    protected abstract String render(String source);

    protected void assertRendering(String source, String expectedResult) {
        Asserts.assertRendering(source, expectedResult, render(source));
    }
}
