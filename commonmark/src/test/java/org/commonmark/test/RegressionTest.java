package org.commonmark.test;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.testutil.RenderingTestCase;
import org.commonmark.testutil.TestResources;
import org.commonmark.testutil.example.Example;
import org.commonmark.testutil.example.ExampleReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ParameterizedClass
@MethodSource("data")
public class RegressionTest extends RenderingTestCase {

    private static final Parser PARSER = Parser.builder().build();
    // The spec says URL-escaping is optional, but the examples assume that it's enabled.
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder().percentEncodeUrls(true).build();

    private static final Map<String, String> OVERRIDDEN_EXAMPLES = getOverriddenExamples();

    @Parameter
    Example example;

    static List<Example> data() {
        var data = new ArrayList<Example>();
        for (var regressionResource : TestResources.getRegressions()) {
            data.addAll(ExampleReader.readExamples(regressionResource));
        }
        return data;
    }

    @Test
    public void testHtmlRendering() {
        String expectedHtml = OVERRIDDEN_EXAMPLES.get(example.getSource());
        if (expectedHtml == null) {
            expectedHtml = example.getHtml();
        }
        assertRendering(example.getSource(), expectedHtml);
    }

    @Override
    protected String render(String source) {
        return RENDERER.render(PARSER.parse(source));
    }

    private static Map<String, String> getOverriddenExamples() {
        Map<String, String> m = new HashMap<>();

        // The only difference is that we don't change `%28` and `%29` to `(` and `)` (percent encoding is preserved)
        m.put("[XSS](javascript&amp;colon;alert%28&#039;XSS&#039;%29)\n",
                "<p><a href=\"javascript&amp;colon;alert%28'XSS'%29\">XSS</a></p>\n");
        // Callers should handle BOMs
        m.put("\uFEFF# Hi\n", "<p>\uFEFF# Hi</p>\n");

        return m;
    }
}
