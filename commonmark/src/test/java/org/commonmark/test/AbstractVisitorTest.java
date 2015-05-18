package org.commonmark.test;

import org.commonmark.node.*;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AbstractVisitorTest {

    @Test
    public void replacingNodeInVisitorShouldNotDestroyVisitOrder() {
        Visitor visitor = new AbstractVisitor() {
            @Override
            public void visit(Text text) {
                text.insertAfter(new Code(text.getLiteral()));
                text.unlink();
            }
        };

        Paragraph paragraph = new Paragraph();
        paragraph.appendChild(new Text("foo"));
        paragraph.appendChild(new Text("bar"));

        paragraph.accept(visitor);

        assertCode("foo", paragraph.getFirstChild());
        assertCode("bar", paragraph.getFirstChild().getNext());
        assertNull(paragraph.getFirstChild().getNext().getNext());
        assertCode("bar", paragraph.getLastChild());
    }

    private static void assertCode(String expectedLiteral, Node node) {
        assertEquals("Expected node to be a Code node: " + node, Code.class, node.getClass());
        Code code = (Code) node;
        assertEquals(expectedLiteral, code.getLiteral());
    }
}
