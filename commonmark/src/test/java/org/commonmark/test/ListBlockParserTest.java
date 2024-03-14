package org.commonmark.test;

import org.commonmark.node.ListItem;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ListBlockParserTest {

    private static final Parser PARSER = Parser.builder().build();

    @Test
    public void testBulletListIndents() {
        assertListItemIndents("* foo", 0, 2);
        assertListItemIndents(" * foo", 1, 3);
        assertListItemIndents("  * foo", 2, 4);
        assertListItemIndents("   * foo", 3, 5);

        assertListItemIndents("*  foo", 0, 3);
        assertListItemIndents("*   foo", 0, 4);
        assertListItemIndents("*    foo", 0, 5);
        assertListItemIndents(" *  foo", 1, 4);
        assertListItemIndents("   *    foo", 3, 8);

        // The indent is relative to any containing blocks
        assertListItemIndents("> * foo", 0, 2);
        assertListItemIndents(">  * foo", 1, 3);
        assertListItemIndents(">  *  foo", 1, 4);

        // Tab counts as 3 spaces here (to the next tab stop column of 4) -> content indent is 1+3
        assertListItemIndents("*\tfoo", 0, 4);

        // Empty list, content indent is expected to be 2
        assertListItemIndents("-\n", 0, 2);
    }

    @Test
    public void testOrderedListIndents() {
        assertListItemIndents("1. foo", 0, 3);
        assertListItemIndents(" 1. foo", 1, 4);
        assertListItemIndents("  1. foo", 2, 5);
        assertListItemIndents("   1. foo", 3, 6);

        assertListItemIndents("1.  foo", 0, 4);
        assertListItemIndents("1.   foo", 0, 5);
        assertListItemIndents("1.    foo", 0, 6);
        assertListItemIndents(" 1.  foo", 1, 5);
        assertListItemIndents("  1.    foo", 2, 8);

        assertListItemIndents("> 1. foo", 0, 3);
        assertListItemIndents(">  1. foo", 1, 4);
        assertListItemIndents(">  1.  foo", 1, 5);

        assertListItemIndents("1.\tfoo", 0, 4);
    }

    private void assertListItemIndents(String input, int expectedMarkerIndent, int expectedContentIndent) {
        Node doc = PARSER.parse(input);
        ListItem listItem = Nodes.find(doc, ListItem.class);
        assertEquals(expectedMarkerIndent, (int) listItem.getMarkerIndent());
        assertEquals(expectedContentIndent, (int) listItem.getContentIndent());
    }
}
