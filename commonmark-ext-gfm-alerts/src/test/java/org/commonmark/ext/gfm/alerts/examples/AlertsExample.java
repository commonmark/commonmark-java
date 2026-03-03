package org.commonmark.ext.gfm.alerts.examples;

import org.commonmark.Extension;
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

        // Create the alerts extension with default settings
        Extension extension = AlertsExtension.create();

        Parser parser = Parser.builder()
                .extensions(List.of(extension))
                .build();

        HtmlRenderer renderer = HtmlRenderer.builder()
                .extensions(List.of(extension))
                .build();

        // Example markdown with all standard alert types
        String markdown = "# GFM Alerts Demo\n\n" +
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

        String html = renderer.render(parser.parse(markdown));

        System.out.println("Markdown Input:");
        System.out.println(markdown);
        System.out.println("\nHTML Output:");
        System.out.println(html);
    }

    private static void customTypesExample() {
        System.out.println("CUSTOM ALERT TYPES");
        System.out.println("=".repeat(60));

        // Create extension with custom types
        Extension extension = AlertsExtension.builder()
                .addCustomType("INFO", "Information")
                .addCustomType("SUCCESS", "Success")
                .addCustomType("DANGER", "Danger")
                .build();

        Parser parser = Parser.builder()
                .extensions(List.of(extension))
                .build();

        HtmlRenderer renderer = HtmlRenderer.builder()
                .extensions(List.of(extension))
                .build();

        // Example markdown with custom alert types
        String markdown = "# Custom Alert Types\n\n" +
                "> [!INFO]\n" +
                "> This is a custom information alert.\n\n" +
                "> [!SUCCESS]\n" +
                "> Operation completed successfully!\n\n" +
                "> [!DANGER]\n" +
                "> This action is dangerous and irreversible.\n\n" +
                "> [!NOTE]\n" +
                "> Standard types still work alongside custom types.\n";

        String html = renderer.render(parser.parse(markdown));

        System.out.println("Markdown Input:");
        System.out.println(markdown);
        System.out.println("\nHTML Output:");
        System.out.println(html);
    }
}
