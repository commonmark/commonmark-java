package org.commonmark.test;

import org.junit.Test;

public class SpecialInputTest extends RenderingTestCase {

    @Test
    public void nullCharacterShouldBeReplaced() {
        assertRendering("foo\0bar", "<p>foo\uFFFDbar</p>\n");
    }

    @Test
    public void nullCharacterEntityShouldBeReplaced() {
        assertRendering("foo&#0;bar", "<p>foo\uFFFDbar</p>\n");
    }

    @Test
    public void crLfAsLineSeparatorShouldBeParsed() {
        assertRendering("foo\r\nbar", "<p>foo\nbar</p>\n");
    }

    @Test
    public void crLfAtEndShouldBeParsed() {
        assertRendering("foo\r\n", "<p>foo</p>\n");
    }

    @Test
    public void surrogatePair() {
        assertRendering("surrogate pair: \uD834\uDD1E", "<p>surrogate pair: \uD834\uDD1E</p>\n");
    }

    @Test
    public void surrogatePairInLinkDestination() {
        assertRendering("[title](\uD834\uDD1E)", "<p><a href=\"\uD834\uDD1E\">title</a></p>\n");
    }

}
