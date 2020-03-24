package org.commonmark.ext.styles;

import org.commonmark.Extension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.testutil.RenderingTestCase;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;

public class StylesTest extends RenderingTestCase {

    private static final Set<Extension> EXTENSIONS = Collections.singleton(StylesExtension.create());
    private static final Parser PARSER = Parser.builder().extensions(EXTENSIONS).build();
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder().extensions(EXTENSIONS).build();

    @Test
    public void baseCase() {
        assertRendering("![text](/url.png){height=5}",
                "<p><img src=\"/url.png\" alt=\"text\" height=\"5\" /></p>\n");

        assertRendering("![text](/url.png){height=5 width=6}",
                "<p><img src=\"/url.png\" alt=\"text\" height=\"5\" width=\"6\" /></p>\n");

        assertRendering("![text](/url.png){height=99px width=100px}",
                "<p><img src=\"/url.png\" alt=\"text\" height=\"99px\" width=\"100px\" /></p>\n");

        assertRendering("![text](/url.png){width=100 height=100}",
                "<p><img src=\"/url.png\" alt=\"text\" width=\"100\" height=\"100\" /></p>\n");

        assertRendering("![text](/url.png){height=4.8 width=3.14}",
                "<p><img src=\"/url.png\" alt=\"text\" height=\"4.8\" width=\"3.14\" /></p>\n");

        assertRendering("![text](/url.png){Width=18 HeIgHt=1001}",
                "<p><img src=\"/url.png\" alt=\"text\" Width=\"18\" HeIgHt=\"1001\" /></p>\n");

        assertRendering("![text](/url.png){height=green width=blue}",
                "<p><img src=\"/url.png\" alt=\"text\" height=\"green\" width=\"blue\" /></p>\n");
    }

    @Test
    public void doubleDelimiters() {
        assertRendering("![text](/url.png){{height=5}}",
                "<p><img src=\"/url.png\" alt=\"text\" height=\"5\" /></p>\n");
    }

    @Test
    public void mismatchingDelimitersAreIgnored() {
        assertRendering("![text](/url.png){", "<p><img src=\"/url.png\" alt=\"text\" />{</p>\n");
    }

    @Test
    public void unsupportedStyleNamesAreRemoved() {
        assertRendering("![text](/url.png){j=502 K=101 img=2 url=5}", "<p><img src=\"/url.png\" alt=\"text\" /></p>\n");
    }

    @Test
    public void repeatedStyleNameUsesFinalOne() {
        assertRendering("![text](/url.png){height=4 height=5 width=1 height=6}",
                "<p><img src=\"/url.png\" alt=\"text\" height=\"6\" width=\"1\" /></p>\n");    }

    @Test
    public void styleValuesAreEscaped() {
        assertRendering("![text](/url.png){height=<img}",
                "<p><img src=\"/url.png\" alt=\"text\" height=\"&lt;img\" /></p>\n");
        assertRendering("![text](/url.png){height=\"\"img}",
                "<p><img src=\"/url.png\" alt=\"text\" height=\"&quot;&quot;img\" /></p>\n");
    }

    @Test
    public void imageAltTextWithSpaces() {
        assertRendering("![Android SDK Manager](/contrib/android-sdk-manager.png){height=502 width=101}",
                "<p><img src=\"/contrib/android-sdk-manager.png\" alt=\"Android SDK Manager\" height=\"502\" width=\"101\" /></p>\n");
    }

    @Test
    public void imageAltTextWithSoftLineBreak() {
        assertRendering("![foo\nbar](/url){height=101 width=202}\n",
                "<p><img src=\"/url\" alt=\"foo\nbar\" height=\"101\" width=\"202\" /></p>\n");
    }

    @Test
    public void imageAltTextWithHardLineBreak() {
        assertRendering("![foo  \nbar](/url){height=506 width=1}\n",
                "<p><img src=\"/url\" alt=\"foo\nbar\" height=\"506\" width=\"1\" /></p>\n");
    }

    @Test
    public void imageAltTextWithEntities() {
        assertRendering("![foo &auml;](/url){height=99 width=100}\n",
                "<p><img src=\"/url\" alt=\"foo \u00E4\" height=\"99\" width=\"100\" /></p>\n");
    }

    @Test
    public void textNodesAreUnchanged() {
        assertRendering("This is some text with random styles immediately afterwards{height=20}\n",
                "<p>This is some text with random styles immediately afterwards</p>\n");
        assertRendering("This is some text with random styles after a space {width=100px}\n",
                "<p>This is some text with random styles after a space </p>\n");
    }

    @Override
    protected String render(String source) {
        return RENDERER.render(PARSER.parse(source));
    }
}
