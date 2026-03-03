package org.commonmark.ext.gfm.alerts;

import org.commonmark.Extension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AlertsTest {

    private static final Set<Extension> EXTENSIONS = Set.of(AlertsExtension.create());
    private static final Parser PARSER = Parser.builder().extensions(EXTENSIONS).build();

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