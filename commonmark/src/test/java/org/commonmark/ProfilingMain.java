package org.commonmark;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.testutil.TestResources;

import java.util.ArrayList;
import java.util.List;

public class ProfilingMain {

    private static final String SPEC = TestResources.readAsString(TestResources.getSpec());
    //    private static final List<String> SPEC_EXAMPLES = ExampleReader.readExampleSources(TestResources.getSpec());
    private static final Parser PARSER = Parser.builder().build();
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder().build();

    public static void main(String[] args) throws Exception {
        System.out.println("Attach profiler, then press enter to start parsing.");
        System.in.read();
        System.out.println("Parsing");
        List<Node> nodes = parse(List.of(SPEC));
        System.out.println("Finished parsing, press enter to start rendering");
        System.in.read();
        System.out.println(render(nodes));
        System.out.println("Finished rendering");
    }

    private static List<Node> parse(List<String> examples) {
        List<Node> nodes = new ArrayList<>();
        for (String example : examples) {
            Node doc = PARSER.parse(example);
            nodes.add(doc);
        }
        return nodes;
    }

    private static long render(List<Node> examples) {
        long length = 0;
        for (Node example : examples) {
            String result = RENDERER.render(example);
            length += result.length();
        }
        return length;
    }
}
