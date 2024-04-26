package org.commonmark.integration;

import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.front.matter.YamlFrontMatterExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.image.attributes.ImageAttributesExtension;
import org.commonmark.ext.ins.InsExtension;
import org.commonmark.ext.task.list.items.TaskListItemsExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.markdown.MarkdownRenderer;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class MarkdownRendererIntegrationTest {

    private static final List<Extension> EXTENSIONS = List.of(
            AutolinkExtension.create(),
            ImageAttributesExtension.create(),
            InsExtension.create(),
            StrikethroughExtension.create(),
            TablesExtension.create(),
            TaskListItemsExtension.create(),
            YamlFrontMatterExtension.create());
    private static final Parser PARSER = Parser.builder().extensions(EXTENSIONS).build();
    private static final MarkdownRenderer RENDERER = MarkdownRenderer.builder().extensions(EXTENSIONS).build();

    @Test
    public void testStrikethroughInTable() {
        assertRoundTrip("|Abc|\n|---|\n|~strikethrough~|\n|\\~escaped\\~|\n");
    }

    private String render(String source) {
        return RENDERER.render(PARSER.parse(source));
    }

    private void assertRoundTrip(String input) {
        String rendered = render(input);
        assertEquals(input, rendered);
    }
}
