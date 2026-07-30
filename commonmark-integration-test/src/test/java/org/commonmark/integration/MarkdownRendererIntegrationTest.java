package org.commonmark.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.markdown.MarkdownRenderer;
import org.junit.jupiter.api.Test;

public class MarkdownRendererIntegrationTest {

    private static final Parser PARSER =
            Parser.builder().extensions(Extensions.ALL_EXTENSIONS).build();
    private static final MarkdownRenderer RENDERER =
            MarkdownRenderer.builder().extensions(Extensions.ALL_EXTENSIONS).build();

    @Test
    public void testStrikethroughInTable() {
        assertRoundTrip("|Abc|\n|---|\n|~strikethrough~|\n|\\~escaped\\~|\n");
    }

    private String render(String source) {
        return RENDERER.render(PARSER.parse(source));
    }

    private void assertRoundTrip(String input) {
        String rendered = render(input);
        assertThat(rendered).isEqualTo(input);
    }
}
