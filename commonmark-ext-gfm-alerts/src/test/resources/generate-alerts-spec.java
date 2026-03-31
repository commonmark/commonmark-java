///usr/bin/env jbang "$0" "$@" ; exit $?

// Generates alerts-spec.txt from alerts-spec-template.md by rendering each example
// through the GitHub Markdown API and inserting the normalized HTML expectation.
//
// Prerequisites: gh CLI installed and authenticated (gh auth login)
// Usage: cd commonmark-ext-gfm-alerts/src/test/resources && jbang generate-alerts-spec.java

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

class GenerateAlertsSpec {

    private static final String FENCE = "````````````````````````````````";
    private static final String EXAMPLE_OPEN = FENCE + " example alert";

    public static void main(String[] args) throws Exception {
        var templatePath = Path.of("alerts-spec-template.md");
        if (!Files.exists(templatePath)) {
            System.err.println("Run from the directory containing alerts-spec-template.md");
            System.exit(1);
        }

        var lines = Files.readAllLines(templatePath);
        var output = new ArrayList<String>();
        var header = "Expectations verified against GitHub Markdown API (gh api markdown -f mode=gfm).\n" +
                "Our HTML omits GitHub's SVG icons and uses a `data-alert-type` attribute instead.";

        int exampleCount = 0;
        int i = 0;
        while (i < lines.size()) {
            var line = lines.get(i);

            // Insert header after the first heading
            if (i == 0 && line.startsWith("# ")) {
                output.add(line);
                output.add("");
                output.add(header);
                i++;
                continue;
            }

            if (line.equals(EXAMPLE_OPEN)) {
                // Collect source lines until closing fence
                output.add(line);
                i++;
                var sourceLines = new ArrayList<String>();
                while (i < lines.size() && !lines.get(i).equals(FENCE)) {
                    sourceLines.add(lines.get(i));
                    output.add(lines.get(i));
                    i++;
                }

                // Render via GitHub API (→ represents tabs in the spec format)
                var source = String.join("\n", sourceLines).replace("\u2192", "\t");
                exampleCount++;
                System.out.printf("%d: %s%n", exampleCount,
                        source.substring(0, Math.min(40, source.length())).replace("\n", "\\n"));

                var ghHtml = normalizeHtml(renderViaGh(source));

                // Insert separator and HTML expectation
                output.add(".");
                output.add(ghHtml);
                output.add(FENCE);
                i++; // skip closing fence from template
            } else {
                output.add(line);
                i++;
            }
        }

        var specPath = Path.of("alerts-spec.txt");
        Files.writeString(specPath, String.join("\n", output) + "\n");
        System.out.println("Done — " + exampleCount + " examples written to alerts-spec.txt");
    }

    static String renderViaGh(String markdown) throws Exception {
        var process = new ProcessBuilder("gh", "api", "markdown", "-f", "mode=gfm", "-f", "text=" + markdown)
                .redirectErrorStream(true)
                .start();
        var output = new String(process.getInputStream().readAllBytes());
        if (process.waitFor() != 0) {
            throw new RuntimeException("gh api failed: " + output);
        }
        return output;
    }

    // Normalize GitHub API HTML to match our renderer output.
    static String normalizeHtml(String html) {
        // Strip GitHub-specific elements and attributes
        html = Pattern.compile("<svg[^>]*>.*?</svg>", Pattern.DOTALL).matcher(html).replaceAll("");
        html = html.replaceAll(" (dir=\"auto\"|rel=\"nofollow\"|class=\"notranslate\")", "");
        // Add data-alert-type and insert newlines to match our renderer's formatting
        html = Pattern.compile("class=\"markdown-alert markdown-alert-(\\w+)\"")
                .matcher(html)
                .replaceAll("class=\"markdown-alert markdown-alert-$1\" data-alert-type=\"$1\"");
        html = Pattern.compile("(data-alert-type=\"\\w+\">)(<p)")
                .matcher(html).replaceAll("$1\n$2");
        // Our renderer outputs one tag per line (htmlWriter.line()), GitHub inlines them
        html = html.replace("</p><p>", "</p>\n<p>");
        return html.replace("\r\n", "\n").lines()
                .map(String::stripTrailing)
                .reduce((a, b) -> a + "\n" + b)
                .orElse("")
                .strip();
    }
}