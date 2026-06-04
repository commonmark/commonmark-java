package org.commonmark.ext.gfm.alerts;

import org.commonmark.Extension;
import org.commonmark.node.Emphasis;
import org.commonmark.node.SourceSpan;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Text;
import org.commonmark.parser.IncludeSourceSpans;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.testutil.RenderingTestCase;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AlertsTest extends RenderingTestCase {

    private static final Set<Extension> EXTENSIONS = Set.of(AlertsExtension.create());
    private static final Parser PARSER = Parser.builder().extensions(EXTENSIONS).includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES).build();
    private static final HtmlRenderer HTML_RENDERER = HtmlRenderer.builder().extensions(EXTENSIONS).build();

    private static final Set<Extension> EXTENSIONS_CUSTOM_TITLES = Set.of(AlertsExtension.builder().allowCustomTitles(true).build());
    private static final Parser PARSER_CUSTOM_TITLES = Parser.builder()
                                                             .extensions(EXTENSIONS_CUSTOM_TITLES)
                                                             .includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES)
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
        var extension = AlertsExtension.builder()
                .addCustomType("INFO", "Information")
                .build();

        var parser = Parser.builder().extensions(Set.of(extension)).build();
        var renderer = HtmlRenderer.builder().extensions(Set.of(extension)).build();

        assertThat(renderer.render(parser.parse("> [!INFO]\n> Custom alert"))).isEqualTo(
                "<div class=\"markdown-alert markdown-alert-info\" data-alert-type=\"info\">\n" +
                "<p class=\"markdown-alert-title\">Information</p>\n" +
                "<p>Custom alert</p>\n" +
                "</div>\n");
    }

    @Test
    public void multipleCustomTypes() {
        var extension = AlertsExtension.builder()
                .addCustomType("INFO", "Information")
                .addCustomType("SUCCESS", "Success!")
                .addCustomType("DANGER", "Danger!")
                .build();

        var parser = Parser.builder().extensions(Set.of(extension)).build();
        var renderer = HtmlRenderer.builder().extensions(Set.of(extension)).build();

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
        var extension = AlertsExtension.builder()
                .addCustomType("INFO", "Information")
                .build();

        var parser = Parser.builder().extensions(Set.of(extension)).build();
        var renderer = HtmlRenderer.builder().extensions(Set.of(extension)).build();

        assertThat(renderer.render(parser.parse("> [!NOTE]\n> Standard type"))).isEqualTo(
                "<div class=\"markdown-alert markdown-alert-note\" data-alert-type=\"note\">\n" +
                "<p class=\"markdown-alert-title\">Note</p>\n" +
                "<p>Standard type</p>\n" +
                "</div>\n");
    }

    @Test
    public void overrideStandardTypeTitle() {
        var extension = AlertsExtension.builder()
                .addCustomType("NOTE", "Nota")
                .build();

        var parser = Parser.builder().extensions(Set.of(extension)).build();
        var renderer = HtmlRenderer.builder().extensions(Set.of(extension)).build();

        assertThat(renderer.render(parser.parse("> [!NOTE]\n> Localized title"))).isEqualTo(
                "<div class=\"markdown-alert markdown-alert-note\" data-alert-type=\"note\">\n" +
                "<p class=\"markdown-alert-title\">Nota</p>\n" +
                "<p>Localized title</p>\n" +
                "</div>\n");
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

    @Test
    public void removeStandardTypes() {
        var extension = AlertsExtension.builder().removeTypes("NOTE", "TIP").build();
        var parser = Parser.builder().extensions(Set.of(extension)).build();
        var renderer = HtmlRenderer.builder().extensions(Set.of(extension)).build();

        assertThat(renderer.render(parser.parse("> [!NOTE]\n> Regular block quote"))).isEqualTo(
                "<blockquote>\n" +
                "<p>[!NOTE]\n" +
                "Regular block quote</p>\n" +
                "</blockquote>\n");

        assertThat(renderer.render(parser.parse("> [!TIP]\n> Regular block quote"))).isEqualTo(
                "<blockquote>\n" +
                "<p>[!TIP]\n" +
                "Regular block quote</p>\n" +
                "</blockquote>\n");

        assertThat(renderer.render(parser.parse("> [!IMPORTANT]\n> Alert"))).isEqualTo(
                "<div class=\"markdown-alert markdown-alert-important\" data-alert-type=\"important\">\n" +
                "<p class=\"markdown-alert-title\">Important</p>\n" +
                "<p>Alert</p>\n" +
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
        // Alerts with no body are allowed.
        assertRenderingCustomTitles("> [!NOTE] Custom _title_\n>  \n>\n>",
                "<div class=\"markdown-alert markdown-alert-note\" data-alert-type=\"note\">\n" +
                "<p class=\"markdown-alert-title\">Custom <em>title</em></p>\n" +
                "</div>\n");
    }

    @Test
    public void customTitleNoBodyNoSpace() {
        assertRenderingCustomTitles("> [!NOTE] Custom _title_",
                "<div class=\"markdown-alert markdown-alert-note\" data-alert-type=\"note\">\n" +
                "<p class=\"markdown-alert-title\">Custom <em>title</em></p>\n" +
                "</div>\n");
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
                "<div class=\"markdown-alert markdown-alert-note\" data-alert-type=\"note\">\n" +
                "<p class=\"markdown-alert-title\">Note</p>\n" +
                "</div>\n" +
                "<p>Body text</p>\n");
    }

    @Test
    public void noLazyContinuationAfterTitle() {
        assertRenderingCustomTitles("> [!NOTE] Custom title\nBody text",
                "<div class=\"markdown-alert markdown-alert-note\" data-alert-type=\"note\">\n" +
                "<p class=\"markdown-alert-title\">Custom title</p>\n" +
                "</div>\n" +
                "<p>Body text</p>\n");
    }

    // Alert markers take precedence over link reference definitions

    @Test
    public void alertTakesPrecedence() {
        assertRenderingCustomTitles("> [!NOTE]: https://example.com\n> Body text\n\n[!NOTE]",
                "<div class=\"markdown-alert markdown-alert-note\" data-alert-type=\"note\">\n" +
                "<p class=\"markdown-alert-title\">: https://example.com</p>\n" +
                "<p>Body text</p>\n" +
                "</div>\n" +
                "<p>[!NOTE]</p>\n");
    }

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
        var extension = AlertsExtension.builder().allowNestedAlerts(true).build();
        var parser = Parser.builder().extensions(Set.of(extension)).build();
        var renderer = HtmlRenderer.builder().extensions(Set.of(extension)).build();

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

    // AST

    @Test
    public void alertParsedAsAlertNode() {
        var document = PARSER.parse("> [!NOTE]\n> This is a note");
        var firstChild = document.getFirstChild();
        assertThat(firstChild).isInstanceOf(Alert.class);
        var alert = (Alert) firstChild;
        assertThat(alert.getType()).isEqualTo("NOTE");
    }

    @Test
    public void customTypeParsedAsAlertNode() {
        var extension = AlertsExtension.builder()
                .addCustomType("INFO", "Information")
                .build();

        var parser = Parser.builder().extensions(Set.of(extension)).build();

        var document = parser.parse("> [!INFO]\n> Custom alert");
        var alert = (Alert) document.getFirstChild();

        assertThat(alert.getType()).isEqualTo("INFO");
    }

    // Source positions

    @Test
    public void titleSourcePositionPreserved() {
        var source = "> [!NOTE] Custom title\n> Body text";
        var document = PARSER_CUSTOM_TITLES.parse(source);
        var alert = (Alert) document.getFirstChild();
        var title = (AlertTitle) alert.getFirstChild();

        // "Custom title" is at column 10, length 12 in line 0
        assertThat(title.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(0, 10, 10, 12)));
    }

    @Test
    public void titleSourcePositionPreservedBetweenBlocks() {
        var source = "- List\n\n> [!NOTE] Custom title\n> Body text\n\nPlain paragraph";
        var document = PARSER_CUSTOM_TITLES.parse(source);
        var alert = (Alert) document.getFirstChild().getNext();
        var title = (AlertTitle) alert.getFirstChild();

        // "Custom title" is at column 10, length 12 in line 2
        assertThat(title.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(2, 10, 18, 12)));
    }

    @Test
    public void titleSourcePositionWithLeadingAndTrailingSpaces() {
        var source = "> [!NOTE]    Custom title   \n> Body text";
        var document = PARSER_CUSTOM_TITLES.parse(source);
        var alert = (Alert) document.getFirstChild();
        var title = (AlertTitle) alert.getFirstChild();

        // Both leading and trailing spaces are trimmed
        assertThat(title.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(0, 13, 13, 12)));
    }

    @Test
    public void titleWithInlineFormattingSourcePosition() {
        var source = "> [!NOTE] Custom _title_\n> Body text";
        var document = PARSER_CUSTOM_TITLES.parse(source);
        var alert = (Alert) document.getFirstChild();
        var title = (AlertTitle) alert.getFirstChild();

        // "Custom _title_" is at column 10, length 14
        assertThat(title.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(0, 10, 10, 14)));

        // First child: "Custom " text node
        var firstText = title.getFirstChild();
        assertThat(firstText).isInstanceOf(Text.class);
        assertThat(((Text) firstText).getLiteral()).isEqualTo("Custom ");
        assertThat(firstText.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(0, 10, 10, 7)));

        // Second child: emphasis node containing "title"
        var emphasis = firstText.getNext();
        assertThat(emphasis).isInstanceOf(Emphasis.class);
        assertThat(emphasis.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(0, 17, 17, 7)));

        // Text inside emphasis: "title"
        var titleText = emphasis.getFirstChild();
        assertThat(titleText).isInstanceOf(Text.class);
        assertThat(((Text) titleText).getLiteral()).isEqualTo("title");
        assertThat(titleText.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(0, 18, 18, 5)));
    }

    @Test
    public void titleWithNestedInlineFormattingSourcePosition() {
        var source = "> [!NOTE] Text with **bold _and italic_**\n> Body text";
        var document = PARSER_CUSTOM_TITLES.parse(source);
        var alert = (Alert) document.getFirstChild();
        var title = (AlertTitle) alert.getFirstChild();

        // "Custom _title_" is at column 10, length 14
        assertThat(title.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(0, 10, 10, 31)));

        // First child: "Text with " text node
        var firstText = title.getFirstChild();
        assertThat(firstText).isInstanceOf(Text.class);
        assertThat(((Text) firstText).getLiteral()).isEqualTo("Text with ");
        assertThat(firstText.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(0, 10, 10, 10)));

        // Second child: strong emphasis node
        var strong = firstText.getNext();
        assertThat(strong).isInstanceOf(StrongEmphasis.class);
        assertThat(strong.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(0, 20, 20, 21)));

        // Inside strong: "bold " text
        var boldText = strong.getFirstChild();
        assertThat(boldText).isInstanceOf(Text.class);
        assertThat(((Text) boldText).getLiteral()).isEqualTo("bold ");
        assertThat(boldText.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(0, 22, 22, 5)));

        // Inside strong: emphasis node with "and italic"
        var emphasis = boldText.getNext();
        assertThat(emphasis).isInstanceOf(Emphasis.class);
        assertThat(emphasis.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(0, 27, 27, 12)));

        // Text inside emphasis: "and italic"
        var italicText = emphasis.getFirstChild();
        assertThat(italicText).isInstanceOf(Text.class);
        assertThat(((Text) italicText).getLiteral()).isEqualTo("and italic");
        assertThat(italicText.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(0, 28, 28, 10)));
    }

}
