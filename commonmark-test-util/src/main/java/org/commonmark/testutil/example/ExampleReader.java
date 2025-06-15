package org.commonmark.testutil.example;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Reader for files containing examples of CommonMark source and the expected HTML rendering (e.g. spec.txt).
 */
public class ExampleReader {

    private static final Pattern SECTION_PATTERN = Pattern.compile("#{1,6} *(.*)");
    private static final String EXAMPLE_START_MARKER = "```````````````````````````````` example";

    private final InputStream inputStream;
    private final String filename;

    private State state = State.BEFORE;
    private String section;
    // The gfm spec has additional text after the example marker for their additions, e.g. "table"
    private String info = "";
    private StringBuilder source;
    private StringBuilder html;
    private int exampleNumber = 0;

    private List<Example> examples = new ArrayList<>();

    private ExampleReader(InputStream stream, String filename) {
        this.inputStream = stream;
        this.filename = filename;
    }

    public static List<Example> readExamples(URL url) {
        try (InputStream stream = url.openStream()) {
            return new ExampleReader(stream, new File(url.getPath()).getName()).read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Example> readExamples(URL url, String info) {
        var examples = readExamples(url);
        return examples.stream().filter(e -> e.getInfo().contains(info)).collect(Collectors.toList());
    }

    public static List<Object[]> readExampleObjects(URL url, String info) {
        return readExamples(url, info).stream().map(e -> new Object[]{e}).collect(Collectors.toList());
    }

    public static List<String> readExampleSources(URL url) {
        List<Example> examples = ExampleReader.readExamples(url);
        List<String> result = new ArrayList<>();
        for (Example example : examples) {
            result.add(example.getSource());
        }
        return result;
    }

    private List<Example> read() throws IOException {
        resetContents();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                processLine(line);
            }
        }

        return examples;
    }

    private void processLine(String line) {
        switch (state) {
            case BEFORE:
                Matcher matcher = SECTION_PATTERN.matcher(line);
                if (matcher.matches()) {
                    section = matcher.group(1);
                    exampleNumber = 0;
                }
                if (line.startsWith(EXAMPLE_START_MARKER)) {
                    info = line.substring(EXAMPLE_START_MARKER.length()).trim();
                    state = State.SOURCE;
                    exampleNumber++;
                }
                break;
            case SOURCE:
                if (line.equals(".")) {
                    state = State.HTML;
                } else {
                    // examples use "rightwards arrow" to show tab
                    String processedLine = line.replace('\u2192', '\t');
                    source.append(processedLine).append('\n');
                }
                break;
            case HTML:
                if (line.equals("````````````````````````````````")) {
                    state = State.BEFORE;
                    examples.add(new Example(filename, section, info, exampleNumber,
                            source.toString(), html.toString()));
                    resetContents();
                } else {
                    html.append(line).append('\n');
                }
                break;
        }
    }

    private void resetContents() {
        source = new StringBuilder();
        html = new StringBuilder();
    }

    private enum State {
        BEFORE, SOURCE, HTML
    }
}
