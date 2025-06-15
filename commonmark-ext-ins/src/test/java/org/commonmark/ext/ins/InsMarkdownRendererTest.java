package org.commonmark.ext.ins;

import org.commonmark.Extension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.markdown.MarkdownRenderer;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class InsMarkdownRendererTest {

    private static final Set<Extension> EXTENSIONS = Set.of(InsExtension.create());
    private static final Parser PARSER = Parser.builder().extensions(EXTENSIONS).build();
    private static final MarkdownRenderer RENDERER = MarkdownRenderer.builder().extensions(EXTENSIONS).build();

    @Test
    public void testStrikethrough() {
        assertRoundTrip("++foo++\n");

        assertRoundTrip("\\+\\+foo\\+\\+\n");
    }

    protected String render(String source) {
        return RENDERER.render(PARSER.parse(source));
    }

    private void assertRoundTrip(String input) {
        String rendered = render(input);
        assertThat(rendered).isEqualTo(input);
    }
}
