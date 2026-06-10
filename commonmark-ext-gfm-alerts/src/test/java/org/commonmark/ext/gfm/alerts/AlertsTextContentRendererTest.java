package org.commonmark.ext.gfm.alerts;

import org.commonmark.Extension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.text.LineBreakRendering;
import org.commonmark.renderer.text.TextContentRenderer;
import org.commonmark.testutil.Asserts;
import org.junit.jupiter.api.Test;

import java.util.Set;

public class AlertsTextContentRendererTest {

    private static final Set<Extension> EXTENSIONS = Set.of(AlertsExtension.create());
    private static final Parser PARSER = Parser.builder().extensions(EXTENSIONS).build();

    private static final TextContentRenderer COMPACT_RENDERER = TextContentRenderer.builder()
            .extensions(EXTENSIONS).build();
    private static final TextContentRenderer SEPARATE_RENDERER = TextContentRenderer.builder()
            .extensions(EXTENSIONS).lineBreakRendering(LineBreakRendering.SEPARATE_BLOCKS).build();
    private static final TextContentRenderer STRIPPED_RENDERER = TextContentRenderer.builder()
            .extensions(EXTENSIONS).lineBreakRendering(LineBreakRendering.STRIP).build();

    private static final Set<Extension> EXTENSIONS_CUSTOM_TITLES = Set.of(
            AlertsExtension.builder().allowCustomTitles(true).allowNestedAlerts(true).build());
    private static final Parser PARSER_CUSTOM_TITLES = Parser.builder()
            .extensions(EXTENSIONS_CUSTOM_TITLES).build();

    private static final TextContentRenderer COMPACT_RENDERER_CUSTOM = TextContentRenderer.builder()
            .extensions(EXTENSIONS_CUSTOM_TITLES).build();
    private static final TextContentRenderer SEPARATE_RENDERER_CUSTOM = TextContentRenderer.builder()
            .extensions(EXTENSIONS_CUSTOM_TITLES)
            .lineBreakRendering(LineBreakRendering.SEPARATE_BLOCKS).build();
    private static final TextContentRenderer STRIPPED_RENDERER_CUSTOM = TextContentRenderer.builder()
            .extensions(EXTENSIONS_CUSTOM_TITLES)
            .lineBreakRendering(LineBreakRendering.STRIP).build();

    @Test
    public void alertNoBody() {
        var source = "> [!NOTE]";
        assertCompact(source, "Note");
        assertSeparate(source, "Note");
        assertStripped(source, "Note");
    }

    @Test
    public void alertWithBody() {
        var source = "> [!NOTE]\n> Body text";
        assertCompact(source, "Note\nBody text");
        assertSeparate(source, "Note\n\nBody text");
        assertStripped(source, "Note: Body text");
    }

    @Test
    public void alertWithMultilineBody() {
        var source = "> [!WARNING]\n> First line\n> Second line";
        assertCompact(source, "Warning\nFirst line\nSecond line");
        assertSeparate(source, "Warning\n\nFirst line\nSecond line");
        assertStripped(source, "Warning: First line Second line");
    }

    @Test
    public void alertWithParagraphBody() {
        var source = "> [!IMPORTANT]\n>\n> First paragraph\n>\n> Second paragraph";
        assertCompact(source, "Important\nFirst paragraph\nSecond paragraph");
        assertSeparate(source, "Important\n\nFirst paragraph\n\nSecond paragraph");
        assertStripped(source, "Important: First paragraph Second paragraph");
    }

    @Test
    public void multipleAlerts() {
        var source = "> [!NOTE]\n> First\n\n> [!TIP]\n> Second";
        assertCompact(source, "Note\nFirst\nTip\nSecond");
        assertSeparate(source, "Note\n\nFirst\n\nTip\n\nSecond");
        assertStripped(source, "Note: First Tip: Second");
    }

    @Test
    public void alertWithInlineFormatting() {
        var source = "> [!NOTE]\n> This has *emphasis* and **strong**";
        assertCompact(source, "Note\nThis has emphasis and strong");
        assertSeparate(source, "Note\n\nThis has emphasis and strong");
        assertStripped(source, "Note: This has emphasis and strong");
    }

    // Custom titles

    @Test
    public void alertWithCustomTitle() {
        var source = "> [!NOTE] Custom title\n> This has *emphasis* and **strong**";
        assertCompactCustomTitles(source, "Custom title\nThis has emphasis and strong");
        assertSeparateCustomTitles(source, "Custom title\n\nThis has emphasis and strong");
        assertStrippedCustomTitles(source, "Custom title: This has emphasis and strong");
    }

    @Test
    public void alertWithCustomTitleNoBody() {
        var source = "> [!NOTE] Custom _title_";
        assertCompactCustomTitles(source, "Custom title");
        assertSeparateCustomTitles(source, "Custom title");
        assertStrippedCustomTitles(source, "Custom title");
    }

    // Nested alerts

    @Test
    public void alertWithinList() {
        var source = "- > [!WARNING]\n  > This has _emphasis_ and `code`";
        assertCompactCustomTitles(source, "- Warning\n  This has emphasis and \"code\"");
        assertSeparateCustomTitles(source, "- Warning\n  This has emphasis and \"code\"");
        assertStrippedCustomTitles(source, "Warning: This has emphasis and \"code\"");
    }

    @Test
    public void alertWithinBlockQuote() {
        var source = ">\n> > [!WARNING]\n> > This has _emphasis_ and **strong**";
        assertCompactCustomTitles(source, "«Warning\nThis has emphasis and strong»");
        assertSeparateCustomTitles(source, "«Warning\n\nThis has emphasis and strong»");
        assertStrippedCustomTitles(source, "«Warning: This has emphasis and strong»");
    }

    @Test
    public void alertWithinAlert() {
        var source = "> [!NOTE] Custom title\n> Body text\n> > [!WARNING]\n> > Nested body text";
        assertCompactCustomTitles(source, "Custom title\nBody text\nWarning\nNested body text");
        assertSeparateCustomTitles(source, "Custom title\n\nBody text\n\nWarning\n\nNested body text");
        assertStrippedCustomTitles(source, "Custom title: Body text Warning: Nested body text");
    }

    // Helpers

    private void assertCompact(String source, String expected) {
        var doc = PARSER.parse(source);
        var actualRendering = COMPACT_RENDERER.render(doc);
        Asserts.assertRendering(source, expected, actualRendering);
    }

    private void assertSeparate(String source, String expected) {
        var doc = PARSER.parse(source);
        var actualRendering = SEPARATE_RENDERER.render(doc);
        Asserts.assertRendering(source, expected, actualRendering);
    }

    private void assertStripped(String source, String expected) {
        var doc = PARSER.parse(source);
        var actualRendering = STRIPPED_RENDERER.render(doc);
        Asserts.assertRendering(source, expected, actualRendering);
    }

    private void assertCompactCustomTitles(String source, String expected) {
        var doc = PARSER_CUSTOM_TITLES.parse(source);
        var actualRendering = COMPACT_RENDERER_CUSTOM.render(doc);
        Asserts.assertRendering(source, expected, actualRendering);
    }

    private void assertSeparateCustomTitles(String source, String expected) {
        var doc = PARSER_CUSTOM_TITLES.parse(source);
        var actualRendering = SEPARATE_RENDERER_CUSTOM.render(doc);
        Asserts.assertRendering(source, expected, actualRendering);
    }

    private void assertStrippedCustomTitles(String source, String expected) {
        var doc = PARSER_CUSTOM_TITLES.parse(source);
        var actualRendering = STRIPPED_RENDERER_CUSTOM.render(doc);
        Asserts.assertRendering(source, expected, actualRendering);
    }
}
