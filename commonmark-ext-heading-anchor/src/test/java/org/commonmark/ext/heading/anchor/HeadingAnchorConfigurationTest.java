package org.commonmark.ext.heading.anchor;

import org.commonmark.Extension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class HeadingAnchorConfigurationTest {

    private static final Parser PARSER = Parser.builder().build();

    private HtmlRenderer buildRenderer(String defaultId, String prefix, String suffix) {
        Extension ext = HeadingAnchorExtension.builder()
                .defaultId(defaultId)
                .idPrefix(prefix)
                .idSuffix(suffix)
                .build();
        return HtmlRenderer.builder()
                .extensions(Arrays.asList(ext))
                .build();
    }

    @Test
    public void testDefaultConfigurationHasNoAdditions() {
        HtmlRenderer renderer = HtmlRenderer.builder()
                .extensions(Arrays.asList(HeadingAnchorExtension.create()))
                .build();
        assertThat(doRender(renderer, "# "), equalTo("<h1 id=\"id\"></h1>\n"));
    }

    @Test
    public void testDefaultIdWhenNoTextOnHeader() {
        HtmlRenderer renderer = buildRenderer("defid", "", "");
        assertThat(doRender(renderer, "# "), equalTo("<h1 id=\"defid\"></h1>\n"));
    }

    @Test
    public void testPrefixAddedToHeader() {
        HtmlRenderer renderer = buildRenderer("", "pre-", "");
        assertThat(doRender(renderer, "# text"), equalTo("<h1 id=\"pre-text\">text</h1>\n"));
    }

    @Test
    public void testSuffixAddedToHeader() {
        HtmlRenderer renderer = buildRenderer("", "", "-post");
        assertThat(doRender(renderer, "# text"), equalTo("<h1 id=\"text-post\">text</h1>\n"));
    }

    private String doRender(HtmlRenderer renderer, String text) {
        return renderer.render(PARSER.parse(text));
    }

}
