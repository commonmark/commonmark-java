package org.commonmark.experimental;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Measurement(batchSize = 10000, iterations = 5)
@Warmup(batchSize = 10000, iterations = 5)
public class ParseBenchmark {
    public static final String lineToBeParsed = "Some text ![text](/url.png) *another* **point** [link](a.com)";

    private static final Parser regularParser = Parser.builder().build();
    private static final Parser nodeSetupFashionParser = Parser.builder()
            .inlineParserFactory(new InlineParserNodeSetupFactory())
            .build();

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(".*" + ParseBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }

    @Benchmark
    public Node currentRegularParser() {
        return regularParser.parse(lineToBeParsed);
    }

    @Benchmark
    public Node newFashionParser() {
        return nodeSetupFashionParser.parse(lineToBeParsed);
    }

}
