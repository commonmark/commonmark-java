package org.commonmark.test;

import org.commonmark.html.HtmlRenderer;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Node;
import org.commonmark.node.Paragraph;
import org.commonmark.parser.Parser;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UsageExampleTest {

    @Test
    public void parseAndRender() {
        Parser parser = Parser.builder().build();
        Node document = parser.parse("This is *Sparta*");
        HtmlRenderer renderer = HtmlRenderer.builder().escapeHtml(true).build();
        assertEquals("<p>This is <em>Sparta</em></p>\n", renderer.render(document));
    }

    @Test
    public void visitor() {
        Parser parser = Parser.builder().build();
        Node node = parser.parse("...");
        MyVisitor visitor = new MyVisitor();
        node.accept(visitor);
    }

    class MyVisitor extends AbstractVisitor {
        @Override
        public void visit(Paragraph paragraph) {
            // Do something with paragraph (override other methods for other nodes):
            System.out.println(paragraph);
            // Descend into children:
            visitChildren(paragraph);
        }
    }

}
