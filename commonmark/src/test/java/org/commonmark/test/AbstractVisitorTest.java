package org.commonmark.test;

import org.commonmark.node.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AbstractVisitorTest {
    @Test
    public void maxDepthMustBeZeroOrGreater() {
        assertThatThrownBy(() -> new RecordingVisitor(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void maxDepthZeroVisitsOnlyRoot() {
        var paragraph = paragraphTree();
        var visitor = new RecordingVisitor(0);

        paragraph.accept(visitor);

        assertThat(visitor.visited).containsExactly("paragraph");
    }

    @Test
    public void maxDepthOneVisitsDirectChildrenButNotGrandchildren() {
        var paragraph = paragraphTree();
        var visitor = new RecordingVisitor(1);

        paragraph.accept(visitor);

        assertThat(visitor.visited).containsExactly("paragraph", "emphasis", "text:tail");
    }

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

    private static Paragraph paragraphTree() {
        var paragraph = new Paragraph();
        var emphasis = new Emphasis();
        emphasis.appendChild(new Text("nested"));
        paragraph.appendChild(emphasis);
        paragraph.appendChild(new Text("tail"));
        return paragraph;
    }

    private static final class RecordingVisitor extends AbstractVisitor {
        private final List<String> visited = new ArrayList<>();

        private RecordingVisitor(int maxDepth) {
            super(maxDepth);
        }

        @Override
        public void visit(Paragraph paragraph) {
            visited.add("paragraph");
            super.visit(paragraph);
        }

        @Override
        public void visit(Emphasis emphasis) {
            visited.add("emphasis");
            super.visit(emphasis);
        }

        @Override
        public void visit(Text text) {
            visited.add("text:" + text.getLiteral());
            super.visit(text);
        }
    }
}
