package org.commonmark.ext.gfm.strikethrough;

import org.commonmark.Extension;
import org.commonmark.test.RenderingTestCase;
import org.junit.Test;

import java.util.Collections;

public class StrikethroughTest extends RenderingTestCase {

    @Test
    public void oneTildeIsNotEnough() {
        assertRendering("~foo~", "<p>~foo~</p>\n");
    }

    @Test
    public void twoTildesYay() {
        assertRendering("~~foo~~", "<p><del>foo</del></p>\n");
    }

    @Test
    public void fourTildesNope() {
        assertRendering("foo ~~~~", "<p>foo ~~~~</p>\n");
    }

    @Test
    public void unmatched() {
        assertRendering("~~foo", "<p>~~foo</p>\n");
        assertRendering("foo~~", "<p>foo~~</p>\n");
    }

    @Test
    public void threeInnerThree() {
        assertRendering("~~~foo~~~", "<p>~<del>foo</del>~</p>\n");
    }

    @Test
    public void twoInnerThree() {
        assertRendering("~~foo~~~", "<p><del>foo</del>~</p>\n");
    }

    @Test
    public void twoStrikethroughsWithoutSpacing() {
        assertRendering("~~foo~~~~bar~~", "<p><del>foo</del><del>bar</del></p>\n");
    }

    @Test
    public void strikethroughWholeParagraphWithOtherDelimiters() {
        assertRendering("~~Paragraph with *emphasis* and __strong emphasis__~~",
                "<p><del>Paragraph with <em>emphasis</em> and <strong>strong emphasis</strong></del></p>\n");
    }

    @Test
    public void insideBlockQuote() {
        assertRendering("> strike ~~that~~",
                "<blockquote>\n<p>strike <del>that</del></p>\n</blockquote>\n");
    }

    @Override
    protected Iterable<? extends Extension> getExtensions() {
        return Collections.singleton(StrikethroughExtension.create());
    }
}
