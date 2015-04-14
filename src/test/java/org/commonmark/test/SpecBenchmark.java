package org.commonmark.test;

import org.commonmark.HtmlRenderer;
import org.commonmark.Parser;
import org.commonmark.spec.SpecExample;
import org.commonmark.spec.SpecReader;
import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@State(Scope.Benchmark)
public class SpecBenchmark {

    List<SpecExample> examples = getExamples();

    public static void main(String[] args) throws Exception {
        Main.main(args);
    }

    @Benchmark
    public void parseAndRender() throws Exception {
        parseAndRender(examples);
    }

    public void benchmarkOneOff() {
        List<SpecExample> examples = getExamples();

        long expectedLength = parseAndRender(examples);
        long before = System.currentTimeMillis();
        long timedLength = parseAndRender(examples);
        long after = System.currentTimeMillis();

        if (timedLength != expectedLength) {
            throw new IllegalStateException("Woops?");
        }

        System.out.println("Parsed, then rendered " + timedLength + " characters in " +
                (after - before) + " ms");
    }

    private static List<SpecExample> getExamples() {
        InputStream stream = SpecTest.class.getResourceAsStream("/spec.txt");
        if (stream == null) {
            throw new IllegalStateException("Could not load spec.txt classpath resource");
        }
        try (SpecReader reader = new SpecReader(stream)) {
            return reader.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static long parseAndRender(List<SpecExample> examples) {
        long length = 0;
        for (SpecExample example : examples) {
            String result = HtmlRenderer.builder().build()
                    .render(Parser.builder().build().parse(example.getSource()));
            length += result.length();
        }
        return length;
    }

}
