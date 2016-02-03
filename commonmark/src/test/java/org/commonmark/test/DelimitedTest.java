package org.commonmark.test;

import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DelimitedTest {

    @Test
    public void emphasisDelimiters() {
        String input = "* *emphasis* \n"
                + "* **strong** \n"
                + "* _important_ \n"
                + "* __CRITICAL__ \n";

        Parser parser = Parser.builder().build();
        Node document = parser.parse(input);

        final List<Delimited> list = new ArrayList<>();
        Visitor visitor = new AbstractVisitor() {
            @Override
            public void visit(Emphasis node) {
                list.add(node);
            }

            @Override
            public void visit(StrongEmphasis node) {
                list.add(node);
            }
        };
        document.accept(visitor);

        assertEquals(4, list.size());

        Delimited emphasis = list.get(0);
        Delimited strong = list.get(1);
        Delimited important = list.get(2);
        Delimited critical = list.get(3);

        assertEquals("*", emphasis.getOpeningDelimiter());
        assertEquals("*", emphasis.getClosingDelimiter());
        assertEquals("**", strong.getOpeningDelimiter());
        assertEquals("**", strong.getClosingDelimiter());
        assertEquals("_", important.getOpeningDelimiter());
        assertEquals("_", important.getClosingDelimiter());
        assertEquals("__", critical.getOpeningDelimiter());
        assertEquals("__", critical.getClosingDelimiter());
    }
}
