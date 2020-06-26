package org.commonmark.integration;

import org.commonmark.node.Image;
import org.commonmark.node.Node;
import org.commonmark.parser.InlineParser;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Measurement(batchSize = 10000, iterations = 5)
@Warmup(batchSize = 10000, iterations = 5)
public class ParseBenchmark {
    private static final Parser regularParser = Parser.builder().build();

    private static final InlineParser.NodeExtension nodeExtensionWithRegex = new InlineParser.NodeExtension() {
        private final Pattern pattern = Pattern.compile("~(?<title>[a-zA-Z]+)~(?<destination>[\\/a-zA-Z.]+)");

        @Override
        public List<InlineBreakdown> lookup(String inline) {
            List<InlineBreakdown> nodesBreakDown = new ArrayList<>();

            Matcher matcher = pattern.matcher(inline);
            while (matcher.find()) {
                nodesBreakDown.add(InlineBreakdown.of(
                        new Image(matcher.group("destination"), matcher.group("title")),
                        matcher.start(),
                        matcher.end()));
            }
            return nodesBreakDown;
        }
    };
    private static final Parser parserExtension = Parser.builder().nodeExtension(nodeExtensionWithRegex).build();

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(".*" + ParseBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }

    @Benchmark
    public Node parseImageRegularUsage() {
        return regularParser.parse("![text](/url.png)");
    }

    @Benchmark
    public Node parseImageByNodeExtension() {
        return parserExtension.parse("Some text ~image~/url.png");
    }
}
