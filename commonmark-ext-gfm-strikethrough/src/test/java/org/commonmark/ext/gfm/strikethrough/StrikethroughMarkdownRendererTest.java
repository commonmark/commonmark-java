package org.commonmark.ext.gfm.strikethrough;

import org.commonmark.Extension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.markdown.MarkdownRenderer;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class StrikethroughMarkdownRendererTest {

    private static final Set<Extension> EXTENSIONS = Collections.singleton(StrikethroughExtension.create());
    private static final Parser PARSER = Parser.builder().extensions(EXTENSIONS).build();
    private static final MarkdownRenderer RENDERER = MarkdownRenderer.builder().extensions(EXTENSIONS).build();

    @Test
    public void testStrikethrough() {
        assertRoundTrip("~foo~ ~bar~\n");
        assertRoundTrip("~~f~oo~~ ~~bar~~\n");

        // TODO this new special character needs to be escaped:
//        assertRoundTrip("\\~foo\\~\n");
    }

    protected String render(String source) {
        return RENDERER.render(PARSER.parse(source));
    }

    private void assertRoundTrip(String input) {
        String rendered = render(input);
        assertEquals(input, rendered);
    }
}
