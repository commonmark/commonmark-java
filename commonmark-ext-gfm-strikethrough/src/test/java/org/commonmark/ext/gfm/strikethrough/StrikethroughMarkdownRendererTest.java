package org.commonmark.ext.gfm.strikethrough;

import org.commonmark.Extension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.markdown.MarkdownRenderer;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;

public class StrikethroughMarkdownRendererTest {

    private static final Set<Extension> EXTENSIONS = Set.of(StrikethroughExtension.create());
    private static final Parser PARSER = Parser.builder().extensions(EXTENSIONS).build();
    private static final MarkdownRenderer RENDERER = MarkdownRenderer.builder().extensions(EXTENSIONS).build();

    @Test
    public void testStrikethrough() {
        assertRoundTrip("~foo~ ~bar~\n");
        assertRoundTrip("~~foo~~ ~~bar~~\n");
        assertRoundTrip("~~f\\~oo~~ ~~bar~~\n");

        assertRoundTrip("\\~foo\\~\n");
    }

    protected String render(String source) {
        return RENDERER.render(PARSER.parse(source));
    }

    private void assertRoundTrip(String input) {
        String rendered = render(input);
        assertEquals(input, rendered);
    }
}
