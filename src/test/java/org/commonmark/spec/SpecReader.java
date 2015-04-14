package org.commonmark.spec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpecReader implements AutoCloseable {

    private static final Pattern SECTION_PATTERN = Pattern.compile("#{1,6} *(.*)");

    private final InputStream inputStream;

    private State state = State.BEFORE;
    private String section;
    private StringBuilder source;
    private StringBuilder html;
    private int exampleNumber = 0;

    private List<SpecExample> examples = new ArrayList<>();

    public SpecReader(InputStream stream) {
        this.inputStream = stream;
    }

    public List<SpecExample> read() throws IOException {
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

    @Override
    public void close() throws IOException {
        inputStream.close();
    }

    private void processLine(String line) {
        boolean dot = line.equals(".");
        switch (state) {
            case BEFORE:
                Matcher matcher = SECTION_PATTERN.matcher(line);
                if (matcher.matches()) {
                    section = matcher.group(1);
                    exampleNumber = 0;
                }
                if (dot) {
                    state = State.SOURCE;
                    exampleNumber++;
                }
                break;
            case SOURCE:
                if (dot) {
                    state = State.HTML;
                } else {
                    // examples use "rightwards arrow" to show tab
                    String processedLine = line.replace('\u2192', '\t');
                    source.append(processedLine).append('\n');
                }
                break;
            case HTML:
                if (dot) {
                    state = State.BEFORE;
                    examples.add(new SpecExample(section, exampleNumber,
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
