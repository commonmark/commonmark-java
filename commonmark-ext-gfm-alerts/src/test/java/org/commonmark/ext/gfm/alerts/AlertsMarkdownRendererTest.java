package org.commonmark.ext.gfm.alerts;

import org.commonmark.Extension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.markdown.MarkdownRenderer;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class AlertsMarkdownRendererTest {

    private static final Set<Extension> EXTENSIONS = Set.of(AlertsExtension.create());
    private static final Parser PARSER = Parser.builder().extensions(EXTENSIONS).build();
    private static final MarkdownRenderer RENDERER = MarkdownRenderer.builder().extensions(EXTENSIONS).build();

    private static final Set<Extension> EXTENSIONS_CUSTOM_TITLES = Set.of(AlertsExtension.builder().allowCustomTitles(true).build());
    private static final Parser PARSER_CUSTOM_TITLES = Parser.builder()
                                                             .extensions(EXTENSIONS_CUSTOM_TITLES)
                                                             .build();
    private static final MarkdownRenderer RENDERER_CUSTOM_TITLES = MarkdownRenderer.builder()
                                                                                   .extensions(EXTENSIONS_CUSTOM_TITLES)
                                                                                   .build();

    @Test
    public void alertRoundTrip() {
        assertRoundTrip("> [!WARNING]\n> Be careful\n");
    }

    @Test
    public void allStandardTypesRoundTrip() {
        assertRoundTrip("> [!NOTE]\n> Note\n");
        assertRoundTrip("> [!TIP]\n> Tip\n");
        assertRoundTrip("> [!IMPORTANT]\n> Important\n");
        assertRoundTrip("> [!WARNING]\n> Warning\n");
        assertRoundTrip("> [!CAUTION]\n> Caution\n");
    }

    @Test
    public void lowercaseTypeRendersAsUppercase() {
        // Lowercase input gets normalized to uppercase type
        var rendered = RENDERER.render(PARSER.parse("> [!note]\n> Content\n"));
        assertThat(rendered).isEqualTo("> [!NOTE]\n> Content\n");
    }

    @Test
    public void leadingAndTrailingLinesAreRemoved() {
        var rendered = RENDERER.render(PARSER.parse(">\n>  \n>[!NOTE]\n> Content\n>\n>   \n"));
        assertThat(rendered).isEqualTo("> [!NOTE]\n> Content\n");
    }

    @Test
    public void alertWithMultipleParagraphs() {
        var input = "> [!NOTE]\n> First paragraph\n>\n> Second paragraph\n";
        // MarkdownWriter always writes the prefix including trailing space
        var expected = "> [!NOTE]\n> First paragraph\n> \n> Second paragraph\n";
        var rendered = RENDERER.render(PARSER.parse(input));
        assertThat(rendered).isEqualTo(expected);
    }

    @Test
    public void customTypeRoundTrip() {
        var customTypes = new HashMap<>(AlertsExtension.STANDARD_TYPES);
        customTypes.put("INFO", "Information");
        var extension = AlertsExtension.builder()
                .setAllowedTypes(customTypes)
                .build();

        var parser = Parser.builder().extensions(Set.of(extension)).build();
        var renderer = MarkdownRenderer.builder().extensions(Set.of(extension)).build();
        var input = "> [!INFO]\n> Custom type\n";

        assertRoundTrip(input, parser, renderer);
    }

    @Test
    public void alertWithList() {
        var input = "> [!NOTE]\n> Items:\n> \n> - First\n> - Second\n";
        assertRoundTrip(input);
    }

    // Custom titles

    @Test
    public void customTitleRoundTrip() {
        assertRoundTripCustomTitles("> [!WARNING] Custom title\n> Be careful\n");
    }

    @Test
    public void customTitleWithFormattingRoundTrip() {
        assertRoundTripCustomTitles("> [!WARNING] Custom _title **with `formatting`**_\n> Be careful\n");
    }

    @Test
    public void customTitleWithMultipleBlocks() {
        var input = "> [!NOTE]Title\n> First paragraph\n>\n> Second paragraph\n>\n> - > Nested blocks\n";
        // MarkdownWriter always writes the prefix including trailing space
        var expected = "> [!NOTE] Title\n> First paragraph\n> \n> Second paragraph\n> \n> - > Nested blocks\n";
        var rendered = RENDERER_CUSTOM_TITLES.render(PARSER_CUSTOM_TITLES.parse(input));
        assertThat(rendered).isEqualTo(expected);
    }

    // Helpers

    private void assertRoundTrip(String input, Parser parser, MarkdownRenderer renderer) {
        var rendered = renderer.render(parser.parse(input));
        assertThat(rendered).isEqualTo(input);
    }

    private void assertRoundTrip(String input) {
        assertRoundTrip(input, PARSER, RENDERER);
    }

    private void assertRoundTripCustomTitles(String input) {
        assertRoundTrip(input, PARSER_CUSTOM_TITLES, RENDERER_CUSTOM_TITLES);
    }
}
