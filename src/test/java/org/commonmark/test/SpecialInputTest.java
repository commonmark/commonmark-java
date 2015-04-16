package org.commonmark.test;

import org.junit.Test;

public class SpecialInputTest extends RenderingTestCase {

    @Test
    public void surrogatePair() {
        assertRendering("surrogate pair: \uD834\uDD1E", "<p>surrogate pair: \uD834\uDD1E</p>\n");
    }

}
