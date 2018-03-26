package org.commonmark.testutil;

import org.commonmark.testutil.example.Example;
import org.commonmark.testutil.example.ExampleReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.List;

@RunWith(Parameterized.class)
public abstract class SpecTestCase extends RenderingTestCase {

    protected final Example example;

    public SpecTestCase(Example example) {
        this.example = example;
    }

    @Parameters(name = "{0}")
    public static List<Object[]> data() {
        List<Example> examples = ExampleReader.readExamples(TestResources.getSpec());
        List<Object[]> data = new ArrayList<>();
        for (Example example : examples) {
            data.add(new Object[]{example});
        }
        return data;
    }

    @Test
    public void testHtmlRendering() {
        assertRendering(example.getSource(), example.getHtml());
    }

}
