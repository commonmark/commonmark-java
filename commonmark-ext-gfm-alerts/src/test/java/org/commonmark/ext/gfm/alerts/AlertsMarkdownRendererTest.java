package org.commonmark.ext.gfm.alerts;

import org.commonmark.Extension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.markdown.MarkdownRenderer;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class AlertsMarkdownRendererTest {

    private static final Set<Extension> EXTENSIONS = Set.of(AlertsExtension.create());
    private static final Parser PARSER = Parser.builder().extensions(EXTENSIONS).build();
    private static final MarkdownRenderer RENDERER = MarkdownRenderer.builder().extensions(EXTENSIONS).build();

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
        String rendered = RENDERER.render(PARSER.parse("> [!note]\n> Content\n"));
        assertThat(rendered).isEqualTo("> [!NOTE]\n> Content\n");
    }

    @Test
    public void alertWithMultipleParagraphs() {
        String input = "> [!NOTE]\n> First paragraph\n>\n> Second paragraph\n";
        // MarkdownWriter always writes the prefix including trailing space
        String expected = "> [!NOTE]\n> First paragraph\n> \n> Second paragraph\n";
        String rendered = RENDERER.render(PARSER.parse(input));
        assertThat(rendered).isEqualTo(expected);
    }

    @Test
    public void customTypeRoundTrip() {
        Extension extension = AlertsExtension.builder()
                .addCustomType("INFO", "Information")
                .build();

        Parser parser = Parser.builder().extensions(Set.of(extension)).build();
        MarkdownRenderer renderer = MarkdownRenderer.builder().extensions(Set.of(extension)).build();

        String input = "> [!INFO]\n> Custom type\n";
        String rendered = renderer.render(parser.parse(input));
        assertThat(rendered).isEqualTo(input);
    }

    @Test
    public void alertWithList() {
        String input = "> [!NOTE]\n> Items:\n> \n> - First\n> - Second\n";
        String rendered = RENDERER.render(PARSER.parse(input));
        assertThat(rendered).isEqualTo(input);
    }

    private void assertRoundTrip(String input) {
        String rendered = RENDERER.render(PARSER.parse(input));
        assertThat(rendered).isEqualTo(input);
    }
}
