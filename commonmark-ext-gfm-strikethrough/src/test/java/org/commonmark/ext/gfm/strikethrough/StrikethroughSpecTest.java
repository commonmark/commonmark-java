package org.commonmark.ext.gfm.strikethrough;

import org.commonmark.Extension;
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

import java.util.List;
import java.util.Set;

@RunWith(Parameterized.class)
public class StrikethroughSpecTest extends RenderingTestCase {

    private static final Set<Extension> EXTENSIONS = Set.of(StrikethroughExtension.create());
    private static final Parser PARSER = Parser.builder().extensions(EXTENSIONS).build();
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder().extensions(EXTENSIONS).build();

    private final Example example;

    public StrikethroughSpecTest(Example example) {
        this.example = example;
    }

    @Parameters(name = "{0}")
    public static List<Object[]> data() {
        return ExampleReader.readExampleObjects(TestResources.getGfmSpec(), "strikethrough");
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
