package org.commonmark.integration;

import org.commonmark.testutil.spec.SpecReader;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.CommandLineOptions;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;

import java.util.Collections;
import java.util.List;

@State(Scope.Benchmark)
public class PegDownBenchmark {

    private static final String SPEC = SpecReader.readSpec();
    private static final List<String> SPEC_EXAMPLES = SpecReader.readExamplesAsString();
    private static final PegDownProcessor PROCESSOR = new PegDownProcessor(Extensions.FENCED_CODE_BLOCKS);

    public static void main(String[] args) throws Exception {
        Options options = new OptionsBuilder()
                .parent(new CommandLineOptions(args))
                .include(PegDownBenchmark.class.getName() + ".*")
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
            String result = PROCESSOR.markdownToHtml(example);
            length += result.length();
        }
        return length;
    }

}
