package org.commonmark.test;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.testutil.TestResources;
import org.commonmark.testutil.example.ExampleReader;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.CommandLineOptions;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Collections;
import java.util.List;

@State(Scope.Benchmark)
public class SpecBenchmark {

    private static final String SPEC = TestResources.readAsString(TestResources.getSpec());
    private static final List<String> SPEC_EXAMPLES = ExampleReader.readExampleSources(TestResources.getSpec());
    private static final Parser PARSER = Parser.builder().build();
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder().build();

    public static void main(String[] args) throws Exception {
        Options options = new OptionsBuilder()
                .parent(new CommandLineOptions(args))
                .include(SpecBenchmark.class.getName() + ".*")
                .build();
        new Runner(options).run();
    }

    @Benchmark
    public long wholeSpec() {
        return parseAndRender(Collections.singletonList(SPEC));
    }

    @Benchmark
    public long examples() {
        return parseAndRender(SPEC_EXAMPLES);
    }

    private static long parseAndRender(List<String> examples) {
        long length = 0;
        for (String example : examples) {
            String result = RENDERER.render(PARSER.parse(example));
            length += result.length();
        }
        return length;
    }

}
