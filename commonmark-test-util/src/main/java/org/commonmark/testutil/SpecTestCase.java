package org.commonmark.testutil;

import org.commonmark.testutil.spec.SpecExample;
import org.commonmark.testutil.spec.SpecReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.List;

@RunWith(Parameterized.class)
public abstract class SpecTestCase extends RenderingTestCase {

    protected final SpecExample example;

    public SpecTestCase(SpecExample example) {
        this.example = example;
    }

    @Parameters(name = "{0}")
    public static List<Object[]> data() {
        List<SpecExample> examples = SpecReader.readExamples();
        List<Object[]> data = new ArrayList<>();
        for (SpecExample example : examples) {
            data.add(new Object[]{example});
        }
        return data;
    }

    @Test
    public void testHtmlRendering() {
        assertRendering(example.getSource(), example.getHtml());
    }

}
