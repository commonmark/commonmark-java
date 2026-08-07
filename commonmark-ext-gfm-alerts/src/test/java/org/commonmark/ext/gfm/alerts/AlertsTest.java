package org.commonmark.ext.gfm.alerts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import java.util.Set;
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

public class AlertsTest extends RenderingTestCase {

    private static final Set<Extension> EXTENSIONS = Set.of(AlertsExtension.create());
    private static final Parser PARSER =
            Parser.builder()
                    .extensions(EXTENSIONS)
                    .includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES)
                    .build();
    private static final HtmlRenderer HTML_RENDERER =
            HtmlRenderer.builder().extensions(EXTENSIONS).build();

    private static final Set<Extension> EXTENSIONS_CUSTOM_TITLES =
            Set.of(AlertsExtension.builder().allowCustomTitles(true).build());
    private static final Parser PARSER_CUSTOM_TITLES =
            Parser.builder()
                    .extensions(EXTENSIONS_CUSTOM_TITLES)
                    .includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES)
                    .build();
    private static final HtmlRenderer HTML_RENDERER_CUSTOM_TITLES =
            HtmlRenderer.builder().extensions(EXTENSIONS_CUSTOM_TITLES).build();

    @Override
    protected String render(String source) {
        return HTML_RENDERER.render(PARSER.parse(source));
    }

    private void assertRenderingCustomTitles(String source, String expectedResult) {
        assertThat(HTML_RENDERER_CUSTOM_TITLES.render(PARSER_CUSTOM_TITLES.parse(source)))
                .isEqualTo(expectedResult);
    }

    // Custom types

    @Test
    public void customType() {
        var extension = AlertsExtension.builder().addCustomType("INFO", "Information").build();

        var parser = Parser.builder().extensions(Set.of(extension)).build();
        var renderer = HtmlRenderer.builder().extensions(Set.of(extension)).build();

        assertThat(renderer.render(parser.parse("> [!INFO]\n> Custom alert")))
                .isEqualTo(
                        """
                        <div class="markdown-alert markdown-alert-info" data-alert-type="info">
                        <p class="markdown-alert-title">Information</p>
                        <p>Custom alert</p>
                        </div>
                        """);
    }

    @Test
    public void multipleCustomTypes() {
        var extension =
                AlertsExtension.builder()
                        .addCustomType("INFO", "Information")
                        .addCustomType("SUCCESS", "Success!")
                        .addCustomType("DANGER", "Danger!")
                        .build();

        var parser = Parser.builder().extensions(Set.of(extension)).build();
        var renderer = HtmlRenderer.builder().extensions(Set.of(extension)).build();

        var s =
                """
                > [!INFO]
                > Info content

                > [!SUCCESS]
                > Success content

                > [!DANGER]
                > Danger content
                """;
        assertThat(renderer.render(parser.parse(s)))
                .isEqualTo(
                        """
                        <div class="markdown-alert markdown-alert-info" data-alert-type="info">
                        <p class="markdown-alert-title">Information</p>
                        <p>Info content</p>
                        </div>
                        <div class="markdown-alert markdown-alert-success" data-alert-type="success">
                        <p class="markdown-alert-title">Success!</p>
                        <p>Success content</p>
                        </div>
                        <div class="markdown-alert markdown-alert-danger" data-alert-type="danger">
                        <p class="markdown-alert-title">Danger!</p>
                        <p>Danger content</p>
                        </div>
                        """);
    }

    @Test
    public void standardTypesWithCustomConfigured() {
        var extension = AlertsExtension.builder().addCustomType("INFO", "Information").build();

        var parser = Parser.builder().extensions(Set.of(extension)).build();
        var renderer = HtmlRenderer.builder().extensions(Set.of(extension)).build();

        var s =
                """
                > [!NOTE]
                > Standard type
                """;
        assertThat(renderer.render(parser.parse(s)))
                .isEqualTo(
                        """
                        <div class="markdown-alert markdown-alert-note" data-alert-type="note">
                        <p class="markdown-alert-title">Note</p>
                        <p>Standard type</p>
                        </div>
                        """);
    }

    @Test
    public void overrideStandardTypeTitle() {
        var extension = AlertsExtension.builder().addCustomType("NOTE", "Nota").build();

        var parser = Parser.builder().extensions(Set.of(extension)).build();
        var renderer = HtmlRenderer.builder().extensions(Set.of(extension)).build();

        var s =
                """
                > [!NOTE]
                > Localized title
                """;
        assertThat(renderer.render(parser.parse(s)))
                .isEqualTo(
                        """
                        <div class="markdown-alert markdown-alert-note" data-alert-type="note">
                        <p class="markdown-alert-title">Nota</p>
                        <p>Localized title</p>
                        </div>
                        """);
    }

    // Custom type validation

    @Test
    public void customTypeMustBeUppercase() {
        assertThrows(
                IllegalArgumentException.class,
                () -> AlertsExtension.builder().addCustomType("info", "Information").build());
    }

    @Test
    public void customTypeMustNotBeEmpty() {
        assertThrows(
                IllegalArgumentException.class,
                () -> AlertsExtension.builder().addCustomType("", "Title").build());
    }

    @Test
    public void customTypeTitleMustNotBeEmpty() {
        assertThrows(
                IllegalArgumentException.class,
                () -> AlertsExtension.builder().addCustomType("INFO", "").build());
    }

    // Overwriting types

    @Test
    public void overwriteStandardTypes() {
        var allowedTypes = Map.ofEntries(Map.entry("IMPORTANT", "Important"));
        var extension =
                AlertsExtension.builder()
                        .setAllowedTypes(allowedTypes)
                        .addCustomType("BUG", "Known Bug")
                        .build();
        var parser = Parser.builder().extensions(Set.of(extension)).build();
        var renderer = HtmlRenderer.builder().extensions(Set.of(extension)).build();

        var s =
                """
                > [!NOTE]
                > Regular block quote
                """;
        assertThat(renderer.render(parser.parse(s)))
                .isEqualTo(
                        """
                        <blockquote>
                        <p>[!NOTE]
                        Regular block quote</p>
                        </blockquote>
                        """);

        s =
                """
                > [!TIP]
                > Regular block quote
                """;
        assertThat(renderer.render(parser.parse(s)))
                .isEqualTo(
                        """
                        <blockquote>
                        <p>[!TIP]
                        Regular block quote</p>
                        </blockquote>
                        """);

        s =
                """
                > [!IMPORTANT]
                > Alert
                """;
        assertThat(renderer.render(parser.parse(s)))
                .isEqualTo(
                        """
                        <div class="markdown-alert markdown-alert-important" data-alert-type="important">
                        <p class="markdown-alert-title">Important</p>
                        <p>Alert</p>
                        </div>
                        """);

        s =
                """
                > [!BUG]
                > Alert
                """;
        assertThat(renderer.render(parser.parse(s)))
                .isEqualTo(
                        """
                        <div class="markdown-alert markdown-alert-bug" data-alert-type="bug">
                        <p class="markdown-alert-title">Known Bug</p>
                        <p>Alert</p>
                        </div>
                        """);
    }

    // Overwriting types validation

    @Test
    public void overwriteTypesMustBeUppercase() {
        var allowedTypes = Map.ofEntries(Map.entry("info", "Info"));
        assertThrows(
                IllegalArgumentException.class,
                () -> AlertsExtension.builder().setAllowedTypes(allowedTypes).build());
    }

    @Test
    public void overwriteTypesMustNotBeEmpty() {
        var allowedTypes = Map.ofEntries(Map.entry("", "Info"));
        assertThrows(
                IllegalArgumentException.class,
                () -> AlertsExtension.builder().setAllowedTypes(allowedTypes).build());
    }

    @Test
    public void overwriteTypesTitleMustNotBeEmpty() {
        var allowedTypes = Map.ofEntries(Map.entry("INFO", ""));
        assertThrows(
                IllegalArgumentException.class,
                () -> AlertsExtension.builder().setAllowedTypes(allowedTypes).build());
    }

    // Custom titles

    @Test
    public void customTitle() {
        assertRenderingCustomTitles(
                """
                > [!NOTE] Custom title
                > Note with a custom title
                """,
                """
                <div class="markdown-alert markdown-alert-note" data-alert-type="note">
                <p class="markdown-alert-title">Custom title</p>
                <p>Note with a custom title</p>
                </div>
                """);
    }

    @Test
    public void customTitleNoSpace() {
        assertRenderingCustomTitles(
                """
                > [!NOTE]Custom title
                > Note with a custom title
                """,
                """
                <div class="markdown-alert markdown-alert-note" data-alert-type="note">
                <p class="markdown-alert-title">Custom title</p>
                <p>Note with a custom title</p>
                </div>
                """);
    }

    @Test
    public void customTitleExtraSpace() {
        assertRenderingCustomTitles(
                """
                > [!NOTE]            Custom title \s
                >    Note with a custom title
                """,
                """
                <div class="markdown-alert markdown-alert-note" data-alert-type="note">
                <p class="markdown-alert-title">Custom title</p>
                <p>Note with a custom title</p>
                </div>
                """);
    }

    @Test
    public void customTitleNoHardLineBreak() {
        assertRenderingCustomTitles(
                """
                > [!NOTE] Custom title\\
                >    Note with a custom title
                """,
                """
                <div class="markdown-alert markdown-alert-note" data-alert-type="note">
                <p class="markdown-alert-title">Custom title\\</p>
                <p>Note with a custom title</p>
                </div>
                """);
    }

    @Test
    public void customTitleWithComment() {
        assertRenderingCustomTitles(
                """
                > [!NOTE] Custom title <!-- Comment --> \s
                >    Note with a custom title
                """,
                """
                <div class="markdown-alert markdown-alert-note" data-alert-type="note">
                <p class="markdown-alert-title">Custom title <!-- Comment --></p>
                <p>Note with a custom title</p>
                </div>
                """);
    }

    @Test
    public void customTitleWithInlineFormatting() {
        assertRenderingCustomTitles(
                """
                > [!NOTE] Custom _title <ins>with **formatting**</ins>_
                > Note with a custom title
                """,
                """
                <div class="markdown-alert markdown-alert-note" data-alert-type="note">
                <p class="markdown-alert-title">Custom <em>title <ins>with <strong>formatting</strong></ins></em></p>
                <p>Note with a custom title</p>
                </div>
                """);
    }

    @Test
    public void customTitleWithLinkAndCode() {
        assertRenderingCustomTitles(
                """
                > [!IMPORTANT] See [docs](https://example.com) or `run()`
                > Note with a custom title
                """,
                """
                <div class="markdown-alert markdown-alert-important" data-alert-type="important">
                <p class="markdown-alert-title">See <a href="https://example.com">docs</a> or <code>run()</code></p>
                <p>Note with a custom title</p>
                </div>
                """);
    }

    @Test
    public void customTitleLeadingEmptyLines() {
        assertRenderingCustomTitles(
                """
                >
                > \s
                > [!NOTE] Custom **title**
                > Note with a custom title
                """,
                """
                <div class="markdown-alert markdown-alert-note" data-alert-type="note">
                <p class="markdown-alert-title">Custom <strong>title</strong></p>
                <p>Note with a custom title</p>
                </div>
                """);
    }

    @Test
    public void customTitleNoOverlappingInlines() {
        assertRenderingCustomTitles(
                """
                > [!NOTE] Custom _title with **opening `delimiters
                > Note` with** closing delimiters_
                """,
                """
                <div class="markdown-alert markdown-alert-note" data-alert-type="note">
                <p class="markdown-alert-title">Custom _title with **opening `delimiters</p>
                <p>Note` with** closing delimiters_</p>
                </div>
                """);
    }

    @Test
    public void customTitleIgnoresBlockContent() {
        assertRenderingCustomTitles(
                """
                > [!NOTE] ## Custom title looks like an ATX heading
                >But it's not
                """,
                """
                <div class="markdown-alert markdown-alert-note" data-alert-type="note">
                <p class="markdown-alert-title">## Custom title looks like an ATX heading</p>
                <p>But it's not</p>
                </div>
                """);
    }

    @Test
    public void customTitleNoBody() {
        // Alerts with no body are allowed.
        assertRenderingCustomTitles(
                """
                > [!NOTE] Custom _title_
                > \s
                >
                >
                """,
                """
                <div class="markdown-alert markdown-alert-note" data-alert-type="note">
                <p class="markdown-alert-title">Custom <em>title</em></p>
                </div>
                """);
    }

    @Test
    public void customTitleNoBodyNoSpace() {
        assertRenderingCustomTitles(
                """
                > [!NOTE] Custom _title_
                """,
                """
                <div class="markdown-alert markdown-alert-note" data-alert-type="note">
                <p class="markdown-alert-title">Custom <em>title</em></p>
                </div>
                """);
    }

    @Test
    public void onlyTrailingWhitespaceIsNotCustomTitle() {
        assertRenderingCustomTitles(
                """
                > [!NOTE]  \s
                > Body text
                """,
                """
                <div class="markdown-alert markdown-alert-note" data-alert-type="note">
                <p class="markdown-alert-title">Note</p>
                <p>Body text</p>
                </div>
                """);
    }

    // Lazy continuation

    @Test
    public void noLazyContinuationAfterMarker() {
        assertRendering(
                """
                > [!NOTE]
                Body text
                """,
                """
                <div class="markdown-alert markdown-alert-note" data-alert-type="note">
                <p class="markdown-alert-title">Note</p>
                </div>
                <p>Body text</p>
                """);
    }

    @Test
    public void noLazyContinuationAfterTitle() {
        assertRenderingCustomTitles(
                """
                > [!NOTE] Custom title
                Body text
                """,
                """
                <div class="markdown-alert markdown-alert-note" data-alert-type="note">
                <p class="markdown-alert-title">Custom title</p>
                </div>
                <p>Body text</p>
                """);
    }

    // Alert markers take precedence over link reference definitions

    @Test
    public void alertTakesPrecedence() {
        assertRenderingCustomTitles(
                """
                > [!NOTE]: https://example.com
                > Body text

                [!NOTE]
                """,
                """
                <div class="markdown-alert markdown-alert-note" data-alert-type="note">
                <p class="markdown-alert-title">: https://example.com</p>
                <p>Body text</p>
                </div>
                <p>[!NOTE]</p>
                """);
    }

    @Test
    public void alertTakesPrecedenceBefore() {
        assertRenderingCustomTitles(
                """
                > [!NOTE]
                > Body text

                [!NOTE]: https://example.com
                """,
                """
                <div class="markdown-alert markdown-alert-note" data-alert-type="note">
                <p class="markdown-alert-title">Note</p>
                <p>Body text</p>
                </div>
                """);
    }

    @Test
    public void alertTakesPrecedenceAfter() {
        assertRenderingCustomTitles(
                """
                [!NOTE]: https://example.com

                > [!NOTE]
                > Body text
                """,
                """
                <div class="markdown-alert markdown-alert-note" data-alert-type="note">
                <p class="markdown-alert-title">Note</p>
                <p>Body text</p>
                </div>
                """);
    }

    // Nested alerts

    @Test
    public void noNestedAlertsByDefault() {
        assertRendering(
                """
                > [!TIP]
                > Body

                - > [!TIP]
                   > Nested body
                """,
                """
                <div class="markdown-alert markdown-alert-tip" data-alert-type="tip">
                <p class="markdown-alert-title">Tip</p>
                <p>Body</p>
                </div>
                <ul>
                <li>
                <blockquote>
                <p>[!TIP]
                Nested body</p>
                </blockquote>
                </li>
                </ul>
                """);
    }

    @Test
    public void noNestedAlertsByDefaultLeadingEmptyLines() {
        assertRendering(
                """
                >
                >  \s
                > [!TIP]
                > Body

                - > [!TIP]
                   > Nested body
                """,
                """
                <div class="markdown-alert markdown-alert-tip" data-alert-type="tip">
                <p class="markdown-alert-title">Tip</p>
                <p>Body</p>
                </div>
                <ul>
                <li>
                <blockquote>
                <p>[!TIP]
                Nested body</p>
                </blockquote>
                </li>
                </ul>
                """);
    }

    @Test
    public void nestedAlerts() {
        var extension = AlertsExtension.builder().allowNestedAlerts(true).build();
        var parser = Parser.builder().extensions(Set.of(extension)).build();
        var renderer = HtmlRenderer.builder().extensions(Set.of(extension)).build();

        var source =
                """
                > [!TIP]
                > Tip body
                > > [!IMPORTANT]
                > > Important body

                - > [!NOTE]
                  > Nested body
                  >
                  > 1. Ordered list body
                  >
                  >    > [!CAUTION]
                  >    >
                  >    > Deeply nested body
                  """;
        var expected =
                """
                <div class="markdown-alert markdown-alert-tip" data-alert-type="tip">
                <p class="markdown-alert-title">Tip</p>
                <p>Tip body</p>
                <div class="markdown-alert markdown-alert-important" data-alert-type="important">
                <p class="markdown-alert-title">Important</p>
                <p>Important body</p>
                </div>
                </div>
                <ul>
                <li>
                <div class="markdown-alert markdown-alert-note" data-alert-type="note">
                <p class="markdown-alert-title">Note</p>
                <p>Nested body</p>
                <ol>
                <li>
                <p>Ordered list body</p>
                <div class="markdown-alert markdown-alert-caution" data-alert-type="caution">
                <p class="markdown-alert-title">Caution</p>
                <p>Deeply nested body</p>
                </div>
                </li>
                </ol>
                </div>
                </li>
                </ul>
                """;
        assertThat(renderer.render(parser.parse(source))).isEqualTo(expected);
    }

    // AST

    @Test
    public void alertParsedAsAlertNode() {
        var document =
                PARSER.parse(
                        """
                        > [!NOTE]
                        > This is a note
                        """);
        var firstChild = document.getFirstChild();
        assertThat(firstChild).isInstanceOf(Alert.class);
        var alert = (Alert) firstChild;
        assertThat(alert.getType()).isEqualTo("NOTE");
    }

    @Test
    public void customTypeParsedAsAlertNode() {
        var extension = AlertsExtension.builder().addCustomType("INFO", "Information").build();

        var parser = Parser.builder().extensions(Set.of(extension)).build();

        var document = parser.parse("> [!INFO]\n> Custom alert");
        var alert = (Alert) document.getFirstChild();

        assertThat(alert.getType()).isEqualTo("INFO");
    }

    // Source positions

    @Test
    public void titleSourcePositionPreserved() {
        var source =
                """
                > [!NOTE] Custom title
                > Body text
                """;
        var document = PARSER_CUSTOM_TITLES.parse(source);
        var alert = (Alert) document.getFirstChild();
        var title = (AlertTitle) alert.getFirstChild();

        // "Custom title" is at column 10, length 12 in line 0
        assertThat(title.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(0, 10, 10, 12)));
    }

    @Test
    public void titleSourcePositionPreservedBetweenBlocks() {
        var source =
                """
                - List

                > [!NOTE] Custom title
                > Body text

                Plain paragraph
                """;
        var document = PARSER_CUSTOM_TITLES.parse(source);
        var alert = (Alert) document.getFirstChild().getNext();
        var title = (AlertTitle) alert.getFirstChild();

        // "Custom title" is at column 10, length 12 in line 2
        assertThat(title.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(2, 10, 18, 12)));
    }

    @Test
    public void titleSourcePositionWithLeadingAndTrailingSpaces() {
        var source =
                """
                > [!NOTE]    Custom title  \s
                > Body text
                """;
        var document = PARSER_CUSTOM_TITLES.parse(source);
        var alert = (Alert) document.getFirstChild();
        var title = (AlertTitle) alert.getFirstChild();

        // Both leading and trailing spaces are trimmed
        assertThat(title.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(0, 13, 13, 12)));
    }

    @Test
    public void titleWithInlineFormattingSourcePosition() {
        var source =
                """
                > [!NOTE] Custom _title_
                > Body text
                """;
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
        var source =
                """
                > [!NOTE] Text with **bold _and italic_**
                > Body text
                """;
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
