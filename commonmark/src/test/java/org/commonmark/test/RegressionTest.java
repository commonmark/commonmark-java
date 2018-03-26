package org.commonmark.test;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.testutil.RenderingTestCase;
import org.commonmark.testutil.TestResources;
import org.commonmark.testutil.example.Example;
import org.commonmark.testutil.example.ExampleReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@RunWith(Parameterized.class)
public class RegressionTest extends RenderingTestCase {

    private static final Parser PARSER = Parser.builder().build();
    // The spec says URL-escaping is optional, but the examples assume that it's enabled.
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder().percentEncodeUrls(true).build();

    private final Example example;

    public RegressionTest(Example example) {
        this.example = example;
    }

    @Parameters(name = "{0}")
    public static List<Object[]> data() {
        List<Object[]> data = new ArrayList<>();
        for (URL regressionResource : TestResources.getRegressions()) {
            List<Example> examples = ExampleReader.readExamples(regressionResource);
            for (Example example : examples) {
                data.add(new Object[]{example});
            }
        }
        return data;
    }

    @Test
    public void testHtmlRendering() {
        assertRendering(example.getSource(), example.getHtml());
    }

    @Override
    protected String render(String source) {
        return RENDERER.render(PARSER.parse(source));
    }
}
