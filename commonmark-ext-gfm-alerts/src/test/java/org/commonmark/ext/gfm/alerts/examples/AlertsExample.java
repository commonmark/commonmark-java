package org.commonmark.ext.gfm.alerts.examples;

import org.commonmark.ext.gfm.alerts.AlertsExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.util.List;

/**
 * Example demonstrating the use of the GFM Alerts extension.
 */
public class AlertsExample {

    public static void main(String[] args) {
        standardTypesExample();
        System.out.println("\n" + "=".repeat(60) + "\n");
        customTypesExample();
    }

    private static void standardTypesExample() {
        System.out.println("STANDARD GFM ALERT TYPES");
        System.out.println("=".repeat(60));

        var extension = AlertsExtension.create();

        var parser = Parser.builder()
                .extensions(List.of(extension))
                .build();

        var renderer = HtmlRenderer.builder()
                .extensions(List.of(extension))
                .build();

        var markdown = "# GFM Alerts Demo\n\n" +
                "> [!NOTE]\n" +
                "> Highlights information that users should take into account.\n\n" +
                "> [!TIP]\n" +
                "> Helpful advice for doing things better.\n\n" +
                "> [!IMPORTANT]\n" +
                "> Key information users need to know.\n\n" +
                "> [!WARNING]\n" +
                "> Urgent info that needs immediate attention.\n\n" +
                "> [!CAUTION]\n" +
                "> Advises about risks or negative outcomes.\n";

        var html = renderer.render(parser.parse(markdown));

        System.out.println("Markdown Input:");
        System.out.println(markdown);
        System.out.println("\nHTML Output:");
        System.out.println(html);
    }

    private static void customTypesExample() {
        System.out.println("CUSTOM ALERT TYPES");
        System.out.println("=".repeat(60));

        var extension = AlertsExtension.builder()
                .addCustomType("BUG", "Known Bug")
                .build();

        var parser = Parser.builder()
                .extensions(List.of(extension))
                .build();

        var renderer = HtmlRenderer.builder()
                .extensions(List.of(extension))
                .build();

        var markdown = "# Custom Alert Types\n\n" +
                "> [!NOTE]\n" +
                "> Useful information that users should know.\n\n" +
                "> [!TIP]\n" +
                "> Helpful advice for doing things better.\n\n" +
                "> [!BUG]\n" +
                "> This feature has a known issue with large files (see #42).\n";

        var html = renderer.render(parser.parse(markdown));

        System.out.println("Markdown Input:");
        System.out.println(markdown);
        System.out.println("\nHTML Output:");
        System.out.println(html);
    }
}
