package org.commonmark.ext.gfm.alerts;

import org.commonmark.Extension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.testutil.RenderingTestCase;
import org.commonmark.testutil.example.Example;
import org.commonmark.testutil.example.ExampleReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URL;
import java.util.List;
import java.util.Set;

@ParameterizedClass
@MethodSource("data")
public class AlertsSpecTest extends RenderingTestCase {

    private static final Set<Extension> EXTENSIONS = Set.of(AlertsExtension.create());
    private static final Parser PARSER = Parser.builder().extensions(EXTENSIONS).build();
    // Use softbreak("<br>") to match GitHub's rendering for easier comparison with GitHub API output.
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder().extensions(EXTENSIONS).softbreak("<br>\n").build();

    @Parameter
    Example example;

    static List<Example> data() {
        URL spec = AlertsSpecTest.class.getResource("/alerts-spec.txt");
        return ExampleReader.readExamples(spec, "alert");
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