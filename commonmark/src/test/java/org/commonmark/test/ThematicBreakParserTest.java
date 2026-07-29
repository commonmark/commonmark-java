package org.commonmark.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.commonmark.node.ThematicBreak;
import org.commonmark.parser.Parser;
import org.junit.jupiter.api.Test;

public class ThematicBreakParserTest {

    private static final Parser PARSER = Parser.builder().build();

    @Test
    public void testLiteral() {
        assertLiteral("***", "***");
        assertLiteral("-- -", "-- -");
        assertLiteral("  __  __  __  ", "  __  __  __  ");
        assertLiteral("***", "> ***");
    }

    private static void assertLiteral(String expected, String input) {
        var tb = Nodes.find(PARSER.parse(input), ThematicBreak.class);
        assertThat(tb.getLiteral()).isEqualTo(expected);
    }
}
