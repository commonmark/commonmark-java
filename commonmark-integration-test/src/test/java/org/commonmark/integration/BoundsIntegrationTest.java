package org.commonmark.integration;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.testutil.TestResources;
import org.commonmark.testutil.example.Example;
import org.commonmark.testutil.example.ExampleReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;

/**
 * Tests various substrings of the spec examples to check for out of bounds exceptions.
 */
@RunWith(Parameterized.class)
public class BoundsIntegrationTest {

    private static final Parser PARSER = Parser.builder().build();

    protected final String input;

    public BoundsIntegrationTest(String input) {
        this.input = input;
    }

    @Parameterized.Parameters(name = "{0}")
    public static List<Object[]> data() {
        List<Example> examples = ExampleReader.readExamples(TestResources.getSpec());
        List<Object[]> data = new ArrayList<>();
        for (Example example : examples) {
            data.add(new Object[]{example.getSource()});
        }
        return data;
    }

    @Test
    public void testSubstrings() {
        // Check possibly truncated block/inline starts
        for (int i = 1; i < input.length() - 1; i++) {
            parse(input.substring(i));
        }
        // Check possibly truncated block/inline ends
        for (int i = input.length() - 1; i > 1; i--) {
            parse(input.substring(0, i));
        }
    }

    private void parse(String input) {
        try {
            Node parsed = PARSER.parse(input);
            // Parsing should always return a node
            assertNotNull(parsed);
        } catch (Exception e) {
            throw new AssertionError("Parsing failed, input: " + input, e);
        }
    }
}
