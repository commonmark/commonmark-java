package org.commonmark.ext.gfm.tables;

import org.commonmark.Extension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.markdown.MarkdownRenderer;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;

public class TableMarkdownRendererTest {

    private static final Set<Extension> EXTENSIONS = Set.of(TablesExtension.create());
    private static final Parser PARSER = Parser.builder().extensions(EXTENSIONS).build();
    private static final MarkdownRenderer RENDERER = MarkdownRenderer.builder().extensions(EXTENSIONS).build();

    @Test
    public void testHeadNoBody() {
        assertRoundTrip("|Abc|\n|---|\n");
        assertRoundTrip("|Abc|Def|\n|---|---|\n");
        assertRoundTrip("|Abc||\n|---|---|\n");
    }

    @Test
    public void testHeadAndBody() {
        assertRoundTrip("|Abc|\n|---|\n|1|\n");
        assertRoundTrip("|Abc|Def|\n|---|---|\n|1|2|\n");
    }

    @Test
    public void testBodyHasFewerColumns() {
        // Could try not to write empty trailing cells but this is fine too
        assertRoundTrip("|Abc|Def|\n|---|---|\n|1||\n");
    }

    @Test
    public void testAlignment() {
        assertRoundTrip("|Abc|Def|\n|:---|---|\n|1|2|\n");
        assertRoundTrip("|Abc|Def|\n|---|---:|\n|1|2|\n");
        assertRoundTrip("|Abc|Def|\n|:---:|:---:|\n|1|2|\n");
    }

    @Test
    public void testInsideBlockQuote() {
        assertRoundTrip("> |Abc|Def|\n> |---|---|\n> |1|2|\n");
    }

    @Test
    public void testMultipleTables() {
        assertRoundTrip("|Abc|Def|\n|---|---|\n\n|One|\n|---|\n|Only|\n");
    }

    @Test
    public void testEscaping() {
        assertRoundTrip("|Abc|Def|\n|---|---|\n|Pipe in|text \\||\n");
        assertRoundTrip("|Abc|Def|\n|---|---|\n|Pipe in|code `\\|`|\n");
        assertRoundTrip("|Abc|Def|\n|---|---|\n|Inline HTML|<span>Foo\\|bar</span>|\n");
    }

    @Test
    public void testEscaped() {
        // `|` in Text nodes needs to be escaped, otherwise the generated Markdown does not get parsed back as a table
        assertRoundTrip("\\|Abc\\|\n\\|---\\|\n");
    }

    protected String render(String source) {
        return RENDERER.render(PARSER.parse(source));
    }

    private void assertRoundTrip(String input) {
        String rendered = render(input);
        assertEquals(input, rendered);
    }
}
