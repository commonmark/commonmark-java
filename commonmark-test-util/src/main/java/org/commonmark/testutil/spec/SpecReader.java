package org.commonmark.testutil.spec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpecReader {

    private static final Pattern SECTION_PATTERN = Pattern.compile("#{1,6} *(.*)");

    private final InputStream inputStream;

    private State state = State.BEFORE;
    private String section;
    private StringBuilder source;
    private StringBuilder html;
    private int exampleNumber = 0;

    private List<SpecExample> examples = new ArrayList<>();

    private SpecReader(InputStream stream) {
        this.inputStream = stream;
    }

    public static List<SpecExample> readExamples() {
        try (InputStream stream = getSpecInputStream()) {
            return new SpecReader(stream).read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> readExamplesAsString() {
        List<SpecExample> examples = SpecReader.readExamples();
        List<String> result = new ArrayList<>();
        for (SpecExample example : examples) {
            result.add(example.getSource());
        }
        return result;
    }

    public static String readSpec() {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getSpecInputStream(), Charset.forName("UTF-8")))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static InputStream getSpecInputStream() {
        InputStream stream = SpecReader.class.getResourceAsStream("/spec.txt");
        if (stream == null) {
            throw new IllegalStateException("Could not load spec.txt classpath resource");
        }
        return stream;
    }

    private List<SpecExample> read() throws IOException {
        resetContents();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, Charset.forName("UTF-8")))) {
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
                if (line.equals("```````````````````````````````` example")) {
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
