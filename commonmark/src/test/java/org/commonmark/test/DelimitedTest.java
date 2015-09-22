package org.commonmark.test;

import org.commonmark.node.Node;
import org.commonmark.node.Delimited;
import org.commonmark.node.Emphasis;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Visitor;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.parser.Parser;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.commonmark.internal.util.Debugging.log;
import static org.commonmark.internal.util.Debugging.toStringTree;

public class DelimitedTest {

    @Test
    public void one() {

        final Parser.Builder builder = Parser.builder();
        final Parser parser = builder.build();
        final Node document = parser.parse(getText());
        final java.util.List<Delimited> list = new java.util.ArrayList();

        final Visitor visitor = new AbstractVisitor() {
                @Override
                public void visit(Emphasis node) {
                    list.add(node);
                }

                @Override
                public void visit(StrongEmphasis node) {
                    list.add(node);
                }
            };

        //log(toStringTree(document));
        document.accept(visitor);

        assertEquals(4, list.size());

        Delimited emphasis = list.get(0);
        Delimited strong = list.get(1);
        Delimited important = list.get(2);
        Delimited critical = list.get(3);

        assertEquals('*', emphasis.getDelimiterChar());
        assertEquals('*', strong.getDelimiterChar());
        assertEquals('_', important.getDelimiterChar());
        assertEquals('_', critical.getDelimiterChar());

        assertEquals(1, emphasis.getDelimiterCount());
        assertEquals(2, strong.getDelimiterCount());
        assertEquals(1, important.getDelimiterCount());
        assertEquals(2, critical.getDelimiterCount());

   }

    String getText() {
        String s = "";
        s += "* *emphasis* \n";
        s += "* **strong** \n";
        s += "* _important_ \n";
        s += "* __CRITICAL__ \n";
        return s;
    }

}
