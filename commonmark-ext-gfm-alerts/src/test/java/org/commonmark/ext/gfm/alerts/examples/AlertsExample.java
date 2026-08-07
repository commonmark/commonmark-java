package org.commonmark.ext.gfm.alerts.examples;

import java.util.List;
import org.commonmark.ext.gfm.alerts.AlertsExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

/** Example demonstrating the use of the GFM Alerts extension. */
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

        var parser = Parser.builder().extensions(List.of(extension)).build();

        var renderer = HtmlRenderer.builder().extensions(List.of(extension)).build();

        var markdown =
                """
                # GFM Alerts Demo

                > [!NOTE]
                > Highlights information that users should take into account.

                > [!TIP]
                > Helpful advice for doing things better.

                > [!IMPORTANT]
                > Key information users need to know.

                > [!WARNING]
                > Urgent info that needs immediate attention.

                > [!CAUTION]
                > Advises about risks or negative outcomes.
                """;

        var html = renderer.render(parser.parse(markdown));

        System.out.println("Markdown Input:");
        System.out.println(markdown);
        System.out.println("\nHTML Output:");
        System.out.println(html);
    }

    private static void customTypesExample() {
        System.out.println("CUSTOM ALERT TYPES");
        System.out.println("=".repeat(60));

        var extension = AlertsExtension.builder().addCustomType("BUG", "Known Bug").build();

        var parser = Parser.builder().extensions(List.of(extension)).build();

        var renderer = HtmlRenderer.builder().extensions(List.of(extension)).build();

        var markdown =
                """
                # Custom Alert Types

                > [!NOTE]
                > Useful information that users should know.

                > [!TIP]
                > Helpful advice for doing things better.

                > [!BUG]
                > This feature has a known issue with large files (see #42).
                """;

        var html = renderer.render(parser.parse(markdown));

        System.out.println("Markdown Input:");
        System.out.println(markdown);
        System.out.println("\nHTML Output:");
        System.out.println(html);
    }
}
