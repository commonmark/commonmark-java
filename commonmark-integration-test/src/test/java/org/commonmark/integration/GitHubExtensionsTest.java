package org.commonmark.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.footnotes.FootnotesExtension;
import org.commonmark.ext.gfm.alerts.AlertsExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.github.GitHubExtensions;
import org.commonmark.ext.task.list.items.TaskListItemsExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.junit.jupiter.api.Test;

class GitHubExtensionsTest {
    @Test
    void rendersTheSixBundledFeaturesTogether() {
        String markdown =
                """
                | column |
                | ------ |
                | ~~old~~ |

                - [x] See https://example.com

                > [!NOTE]
                > See the note[^one].

                [^one]: A footnote.
                """;
        List<Extension> manual =
                List.of(
                        AutolinkExtension.create(),
                        FootnotesExtension.create(),
                        AlertsExtension.create(),
                        StrikethroughExtension.create(),
                        TablesExtension.create(),
                        TaskListItemsExtension.create());
        List<Extension> preset = GitHubExtensions.create();
        assertEquals(
                manual.stream().map(Object::getClass).toList(),
                preset.stream().map(Object::getClass).toList());
        String html = render(markdown, preset);
        assertEquals(render(markdown, manual), html);
        for (String feature :
                List.of(
                        "<th>column</th>",
                        "<del>old</del>",
                        "type=\"checkbox\"",
                        "<a href=\"https://example.com\">",
                        "markdown-alert-note",
                        "class=\"footnote-ref\"")) {
            assertTrue(html.contains(feature), feature);
        }
    }

    @Test
    void preservesCoreRenderingForOtherExtensions() {
        String markdown =
                """
                ---
                title: example
                ---

                # Heading

                ++insert++ ![text](/img.png){width=6}
                """;
        assertEquals(render(markdown, List.of()), render(markdown, GitHubExtensions.create()));
    }

    @Test
    void callersCannotChangeThePreset() {
        assertThrows(UnsupportedOperationException.class, () -> GitHubExtensions.create().clear());
        assertEquals(6, GitHubExtensions.create().size());
    }

    private static String render(String markdown, List<Extension> extensions) {
        Parser parser = Parser.builder().extensions(extensions).build();
        return HtmlRenderer.builder().extensions(extensions).build().render(parser.parse(markdown));
    }
}
