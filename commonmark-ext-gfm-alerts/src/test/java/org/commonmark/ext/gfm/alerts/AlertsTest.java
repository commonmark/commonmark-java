package org.commonmark.ext.gfm.alerts;

import org.commonmark.Extension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.testutil.RenderingTestCase;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AlertsTest extends RenderingTestCase {

    private static final Set<Extension> EXTENSIONS = Set.of(AlertsExtension.create());
    private static final Parser PARSER = Parser.builder().extensions(EXTENSIONS).build();
    private static final HtmlRenderer HTML_RENDERER = HtmlRenderer.builder().extensions(EXTENSIONS).build();

    private static final Set<Extension> EXTENSIONS_CUSTOM_TITLES = Set.of(AlertsExtension.builder().allowCustomTitles().build());
    private static final Parser PARSER_CUSTOM_TITLES = Parser.builder()
                                                             .extensions(EXTENSIONS_CUSTOM_TITLES)
                                                             .build();
    private static final HtmlRenderer HTML_RENDERER_CUSTOM_TITLES = HtmlRenderer.builder()
                                                                                .extensions(EXTENSIONS_CUSTOM_TITLES)
                                                                                .build();

    @Override
    protected String render(String source) {
        return HTML_RENDERER.render(PARSER.parse(source));
    }

    private void assertRenderingCustomTitles(String source, String expectedResult) {
        assertThat(HTML_RENDERER_CUSTOM_TITLES.render(PARSER_CUSTOM_TITLES.parse(source))).isEqualTo(expectedResult);
    }

    // Custom types

    @Test
    public void customType() {
        Extension extension = AlertsExtension.builder()
                .addCustomType("INFO", "Information")
                .build();

        Parser parser = Parser.builder().extensions(Set.of(extension)).build();
        HtmlRenderer renderer = HtmlRenderer.builder().extensions(Set.of(extension)).build();

        assertThat(renderer.render(parser.parse("> [!INFO]\n> Custom alert"))).isEqualTo(
                "<div class=\"markdown-alert markdown-alert-info\" data-alert-type=\"info\">\n" +
                "<p class=\"markdown-alert-title\">Information</p>\n" +
                "<p>Custom alert</p>\n" +
                "</div>\n");
    }

    @Test
    public void multipleCustomTypes() {
        Extension extension = AlertsExtension.builder()
                .addCustomType("INFO", "Information")
                .addCustomType("SUCCESS", "Success!")
                .addCustomType("DANGER", "Danger!")
                .build();

        Parser parser = Parser.builder().extensions(Set.of(extension)).build();
        HtmlRenderer renderer = HtmlRenderer.builder().extensions(Set.of(extension)).build();

        assertThat(renderer.render(parser.parse("> [!INFO]\n> Info content\n\n> [!SUCCESS]\n> Success content\n\n> [!DANGER]\n> Danger content"))).isEqualTo(
                "<div class=\"markdown-alert markdown-alert-info\" data-alert-type=\"info\">\n" +
                "<p class=\"markdown-alert-title\">Information</p>\n" +
                "<p>Info content</p>\n" +
                "</div>\n" +
                "<div class=\"markdown-alert markdown-alert-success\" data-alert-type=\"success\">\n" +
                "<p class=\"markdown-alert-title\">Success!</p>\n" +
                "<p>Success content</p>\n" +
                "</div>\n" +
                "<div class=\"markdown-alert markdown-alert-danger\" data-alert-type=\"danger\">\n" +
                "<p class=\"markdown-alert-title\">Danger!</p>\n" +
                "<p>Danger content</p>\n" +
                "</div>\n");
    }

    @Test
    public void standardTypesWithCustomConfigured() {
        Extension extension = AlertsExtension.builder()
                .addCustomType("INFO", "Information")
                .build();

        Parser parser = Parser.builder().extensions(Set.of(extension)).build();
        HtmlRenderer renderer = HtmlRenderer.builder().extensions(Set.of(extension)).build();

        assertThat(renderer.render(parser.parse("> [!NOTE]\n> Standard type"))).isEqualTo(
                "<div class=\"markdown-alert markdown-alert-note\" data-alert-type=\"note\">\n" +
                "<p class=\"markdown-alert-title\">Note</p>\n" +
                "<p>Standard type</p>\n" +
                "</div>\n");
    }

    @Test
    public void overrideStandardTypeTitle() {
        Extension extension = AlertsExtension.builder()
                .addCustomType("NOTE", "Nota")
                .build();

        Parser parser = Parser.builder().extensions(Set.of(extension)).build();
        HtmlRenderer renderer = HtmlRenderer.builder().extensions(Set.of(extension)).build();

        assertThat(renderer.render(parser.parse("> [!NOTE]\n> Localized title"))).isEqualTo(
                "<div class=\"markdown-alert markdown-alert-note\" data-alert-type=\"note\">\n" +
                "<p class=\"markdown-alert-title\">Nota</p>\n" +
                "<p>Localized title</p>\n" +
                "</div>\n");
    }

    // Custom titles

    @Test
    public void customTitle() {
        assertRenderingCustomTitles("> [!NOTE] Custom title\n> Note with a custom title",
                "<div class=\"markdown-alert markdown-alert-note\" data-alert-type=\"note\">\n" +
                "<p class=\"markdown-alert-title\">Custom title</p>\n" +
                "<p>Note with a custom title</p>\n" +
                "</div>\n");
    }

    @Test
    public void customTitleNoSpace() {
        assertRenderingCustomTitles("> [!NOTE]Custom title\n> Note with a custom title",
                "<div class=\"markdown-alert markdown-alert-note\" data-alert-type=\"note\">\n" +
                "<p class=\"markdown-alert-title\">Custom title</p>\n" +
                "<p>Note with a custom title</p>\n" +
                "</div>\n");
    }

    @Test
    public void customTitleExtraSpace() {
        assertRenderingCustomTitles("> [!NOTE]            Custom title  \n>    Note with a custom title",
                "<div class=\"markdown-alert markdown-alert-note\" data-alert-type=\"note\">\n" +
                "<p class=\"markdown-alert-title\">Custom title</p>\n" +
                "<p>Note with a custom title</p>\n" +
                "</div>\n");
    }

    @Test
    public void customTitleNoHardLineBreak() {
        assertRenderingCustomTitles("> [!NOTE] Custom title\\\n>    Note with a custom title",
                "<div class=\"markdown-alert markdown-alert-note\" data-alert-type=\"note\">\n" +
                "<p class=\"markdown-alert-title\">Custom title\\</p>\n" +
                "<p>Note with a custom title</p>\n" +
                "</div>\n");
    }

    @Test
    public void customTitleWithComment() {
        assertRenderingCustomTitles("> [!NOTE] Custom title <!-- Comment -->  \n>    Note with a custom title",
                "<div class=\"markdown-alert markdown-alert-note\" data-alert-type=\"note\">\n" +
                "<p class=\"markdown-alert-title\">Custom title <!-- Comment --></p>\n" +
                "<p>Note with a custom title</p>\n" +
                "</div>\n");
    }

    @Test
    public void customTitleWithInlineFormatting() {
        assertRenderingCustomTitles("> [!NOTE] Custom _title <ins>with **formatting**</ins>_\n> Note with a custom title",
                "<div class=\"markdown-alert markdown-alert-note\" data-alert-type=\"note\">\n" +
                "<p class=\"markdown-alert-title\">Custom <em>title <ins>with <strong>formatting</strong></ins></em></p>\n" +
                "<p>Note with a custom title</p>\n" +
                "</div>\n");
    }

    @Test
    public void customTitleWithLinkAndCode() {
        assertRenderingCustomTitles("> [!IMPORTANT] See [docs](https://example.com) or `run()`\n> Note with a custom title",
                "<div class=\"markdown-alert markdown-alert-important\" data-alert-type=\"important\">\n" +
                "<p class=\"markdown-alert-title\">See <a href=\"https://example.com\">docs</a> or <code>run()</code></p>\n" +
                "<p>Note with a custom title</p>\n" +
                "</div>\n");
    }

    @Test
    public void customTitleLeadingEmptyLines() {
        assertRenderingCustomTitles(">\n>  \n> [!NOTE] Custom **title**\n> Note with a custom title",
                "<div class=\"markdown-alert markdown-alert-note\" data-alert-type=\"note\">\n" +
                "<p class=\"markdown-alert-title\">Custom <strong>title</strong></p>\n" +
                "<p>Note with a custom title</p>\n" +
                "</div>\n");
    }

    @Test
    public void customTitleNoOverlappingInlines() {
        assertRenderingCustomTitles("> [!NOTE] Custom _title with **opening `delimiters\n> Note` with** closing delimiters_",
                "<div class=\"markdown-alert markdown-alert-note\" data-alert-type=\"note\">\n" +
                "<p class=\"markdown-alert-title\">Custom _title with **opening `delimiters</p>\n" +
                "<p>Note` with** closing delimiters_</p>\n" +
                "</div>\n");
    }

    @Test
    public void customTitleIgnoresBlockContent() {
        assertRenderingCustomTitles("> [!NOTE] ## Custom title looks like an ATX heading\n>But it's not",
                "<div class=\"markdown-alert markdown-alert-note\" data-alert-type=\"note\">\n" +
                "<p class=\"markdown-alert-title\">## Custom title looks like an ATX heading</p>\n" +
                "<p>But it's not</p>\n" +
                "</div>\n");
    }

    @Test
    public void customTitleNoBody() {
        // Inlines should be parsed as usual after falling back to a block quote.
        assertRenderingCustomTitles("> [!NOTE] Custom _title_\n>  \n>\n>",
                "<blockquote>\n" +
                "<p>[!NOTE] Custom <em>title</em></p>\n" +
                "</blockquote>\n");
    }

    @Test
    public void customTitleNoBodyNoSpace() {
        assertRenderingCustomTitles("> [!NOTE] Custom _title_\n>  \n>\n>",
                "<blockquote>\n" +
                "<p>[!NOTE] Custom <em>title</em></p>\n" +
                "</blockquote>\n");
    }

    @Test
    public void onlyTrailingWhitespaceIsNotCustomTitle() {
        assertRenderingCustomTitles("> [!NOTE]   \n> Body text",
                "<div class=\"markdown-alert markdown-alert-note\" data-alert-type=\"note\">\n" +
                "<p class=\"markdown-alert-title\">Note</p>\n" +
                "<p>Body text</p>\n" +
                "</div>\n");
    }

    // Lazy continuation

    @Test
    public void noLazyContinuationAfterMarker() {
        assertRendering("> [!NOTE]\nBody text",
                "<blockquote>\n" +
                "<p>[!NOTE]</p>\n" +
                "</blockquote>\n" +
                "<p>Body text</p>\n");
    }

    @Test
    public void noLazyContinuationAfterTitle() {
        assertRenderingCustomTitles("> [!NOTE] Custom title\nBody text",
                "<blockquote>\n" +
                "<p>[!NOTE] Custom title</p>\n" +
                "</blockquote>\n" +
                "<p>Body text</p>\n");
    }

    // Alert markers take precedence over link reference definitions

    @Test
    public void alertTakesPrecedenceBefore() {
        assertRenderingCustomTitles("> [!NOTE]\n> Body text\n\n[!NOTE]: https://example.com",
                "<div class=\"markdown-alert markdown-alert-note\" data-alert-type=\"note\">\n" +
                "<p class=\"markdown-alert-title\">Note</p>\n" +
                "<p>Body text</p>\n" +
                "</div>\n");
    }

    @Test
    public void alertTakesPrecedenceAfter() {
        assertRenderingCustomTitles("[!NOTE]: https://example.com\n\n> [!NOTE]\n> Body text",
                "<div class=\"markdown-alert markdown-alert-note\" data-alert-type=\"note\">\n" +
                "<p class=\"markdown-alert-title\">Note</p>\n" +
                "<p>Body text</p>\n" +
                "</div>\n");
    }

    // Nested alerts

    @Test
    public void noNestedAlertsByDefault() {
        assertRendering("> [!TIP]\n> Body\n\n- > [!TIP]\n   > Nested body",
                "<div class=\"markdown-alert markdown-alert-tip\" data-alert-type=\"tip\">\n" +
                "<p class=\"markdown-alert-title\">Tip</p>\n" +
                "<p>Body</p>\n" +
                "</div>\n" +
                "<ul>\n" +
                "<li>\n" +
                "<blockquote>\n" +
                "<p>[!TIP]\n" +
                "Nested body</p>\n" +
                "</blockquote>\n" +
                "</li>\n" +
                "</ul>\n");
    }

    @Test
    public void noNestedAlertsByDefaultLeadingEmptyLines() {
        assertRendering(">\n>   \n> [!TIP]\n> Body\n\n- > [!TIP]\n   > Nested body",
                "<div class=\"markdown-alert markdown-alert-tip\" data-alert-type=\"tip\">\n" +
                "<p class=\"markdown-alert-title\">Tip</p>\n" +
                "<p>Body</p>\n" +
                "</div>\n" +
                "<ul>\n" +
                "<li>\n" +
                "<blockquote>\n" +
                "<p>[!TIP]\n" +
                "Nested body</p>\n" +
                "</blockquote>\n" +
                "</li>\n" +
                "</ul>\n");
    }

    @Test
    public void nestedAlerts() {
        Extension extension = AlertsExtension.builder().allowNestedAlerts().build();
        Parser parser = Parser.builder().extensions(Set.of(extension)).build();
        HtmlRenderer renderer = HtmlRenderer.builder().extensions(Set.of(extension)).build();

        var source = String.join("\n",
                "> [!TIP]",
                "> Tip body",
                "> > [!IMPORTANT]",
                "> > Important body",
                "",
                "- > [!NOTE]",
                "  > Nested body",
                "  >",
                "  > 1. Ordered list body",
                "  >",
                "  >    > [!CAUTION]",
                "  >    >",
                "  >    > Deeply nested body");
        var expected = String.join("\n",
                "<div class=\"markdown-alert markdown-alert-tip\" data-alert-type=\"tip\">",
                "<p class=\"markdown-alert-title\">Tip</p>",
                "<p>Tip body</p>",
                "<div class=\"markdown-alert markdown-alert-important\" data-alert-type=\"important\">",
                "<p class=\"markdown-alert-title\">Important</p>",
                "<p>Important body</p>",
                "</div>",
                "</div>",
                "<ul>",
                "<li>",
                "<div class=\"markdown-alert markdown-alert-note\" data-alert-type=\"note\">",
                "<p class=\"markdown-alert-title\">Note</p>",
                "<p>Nested body</p>",
                "<ol>",
                "<li>",
                "<p>Ordered list body</p>",
                "<div class=\"markdown-alert markdown-alert-caution\" data-alert-type=\"caution\">",
                "<p class=\"markdown-alert-title\">Caution</p>",
                "<p>Deeply nested body</p>",
                "</div>",
                "</li>",
                "</ol>",
                "</div>",
                "</li>",
                "</ul>\n");

        assertThat(renderer.render(parser.parse(source))).isEqualTo(expected);
    }

    // Custom type validation

    @Test
    public void customTypeMustBeUppercase() {
        assertThrows(IllegalArgumentException.class, () ->
                AlertsExtension.builder().addCustomType("info", "Information").build());
    }

    @Test
    public void customTypeMustNotBeEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                AlertsExtension.builder().addCustomType("", "Title").build());
    }

    @Test
    public void customTypeTitleMustNotBeEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                AlertsExtension.builder().addCustomType("INFO", "").build());
    }

    // AST

    @Test
    public void alertParsedAsAlertNode() {
        Node document = PARSER.parse("> [!NOTE]\n> This is a note");
        Node firstChild = document.getFirstChild();
        assertThat(firstChild).isInstanceOf(Alert.class);
        Alert alert = (Alert) firstChild;
        assertThat(alert.getType()).isEqualTo("NOTE");
    }

    @Test
    public void customTypeParsedAsAlertNode() {
        Extension extension = AlertsExtension.builder()
                .addCustomType("INFO", "Information")
                .build();

        Parser parser = Parser.builder().extensions(Set.of(extension)).build();

        Node document = parser.parse("> [!INFO]\n> Custom alert");
        Alert alert = (Alert) document.getFirstChild();

        assertThat(alert.getType()).isEqualTo("INFO");
    }

}
