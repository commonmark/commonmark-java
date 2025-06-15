package org.commonmark.integration;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.testutil.TestResources;
import org.commonmark.testutil.example.ExampleReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests various substrings of the spec examples to check for out of bounds exceptions.
 */
@ParameterizedClass
@MethodSource("data")
public class BoundsIntegrationTest {

    private static final Parser PARSER = Parser.builder().build();

    @Parameter
    String input;

    static List<String> data() {
        return ExampleReader.readExampleSources(TestResources.getSpec());
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
            assertThat(parsed).isNotNull();
        } catch (Exception e) {
            throw new AssertionError("Parsing failed, input: " + input, e);
        }
    }
}
