package org.commonmark.test;

import org.commonmark.node.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(paragraph.getFirstChild().getNext().getNext()).isNull();
        assertCode("bar", paragraph.getLastChild());
    }

    private static void assertCode(String expectedLiteral, Node node) {
        assertThat(node).isInstanceOf(Code.class);
        Code code = (Code) node;
        assertThat(code.getLiteral()).isEqualTo(expectedLiteral);
    }
}
