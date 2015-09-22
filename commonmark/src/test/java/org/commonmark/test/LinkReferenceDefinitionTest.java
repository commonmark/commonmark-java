package org.commonmark.test;

import org.commonmark.node.Node;
import org.commonmark.node.Link;
import org.commonmark.node.AutoLink;
import org.commonmark.node.Visitor;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.parser.Parser;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.commonmark.internal.util.Debugging.log;
import static org.commonmark.internal.util.Debugging.toStringTree;

public class LinkReferenceDefinitionTest {

    @Test
    public void one() {

        final Parser.Builder builder = Parser.builder();
        final Parser parser = builder.build();
        final Node document = parser.parse(getText());
        final java.util.List<Link> links = new java.util.ArrayList();

        final Visitor visitor = new AbstractVisitor() {
                @Override
                public void visit(Link node) {
                    links.add(node);
                }

                @Override
                public void visit(AutoLink node) {
                    links.add(node);
                }
            };

        //log(toStringTree(document));
        document.accept(visitor);

        assertEquals(3, links.size());

        Link one = links.get(0);
        Link two = links.get(1);
        Link auto = links.get(2);

        assertEquals("1", one.getDefinition().getLabel());
        assertEquals("2", two.getDefinition().getLabel());
        assertEquals(AutoLink.class, auto.getClass());
   }

    String getText() {
        String s = "";
        s += "* this is collapsed link: [one][1]\n";
        s += "* this is shortcut link: [2]\n";
        s += "* this is an AutoLink: <http://foo.com> ...\n";
        s += "* not to be confused with the *Autolink plugin*,";
        s += "  that is not picked up in this test: http://foo.com\n";
        s += "\n";
        s += "[1]: http://1.com\n";
        s += "[2]: http://2.com\n";
        return s;
    }

}
