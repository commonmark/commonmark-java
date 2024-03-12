package org.commonmark.test;

import org.commonmark.node.ThematicBreak;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.testutil.RenderingTestCase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
        assertNotNull(tb);
        assertEquals(expected, tb.getLiteral());
    }
}
