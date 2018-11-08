package org.commonmark;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.testutil.TestResources;

import java.util.Collections;
import java.util.List;

public class ProfilingMain {

    private static final String SPEC = TestResources.readAsString(TestResources.getSpec());
    //    private static final List<String> SPEC_EXAMPLES = ExampleReader.readExampleSources(TestResources.getSpec());
    private static final Parser PARSER = Parser.builder().build();
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder().build();

    public static void main(String[] args) throws Exception {
        System.out.println("Started up, attach profiler now");
        Thread.sleep(10_000);
        System.out.println("Parsing and rendering");
        parseAndRender(Collections.singletonList(SPEC));
        System.out.println("Finished parsing");
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
