package org.commonmark.test;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Node;
import org.commonmark.node.Text;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.testutil.SpecTestCase;
import org.commonmark.testutil.example.Example;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.fail;
import static org.commonmark.testutil.Asserts.assertRendering;

public class SpecCoreTest extends SpecTestCase {

    private static final Parser PARSER = Parser.builder().build();
    // The spec says URL-escaping is optional, but the examples assume that it's enabled.
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder().percentEncodeUrls(true).build();

    @Test
    public void testTextNodesContiguous() {
        final String source = example.getSource();
        Node node = PARSER.parse(source);
        node.accept(new AbstractVisitor() {
            @Override
            protected void visitChildren(Node parent) {
                if (parent instanceof Text && parent.getFirstChild() != null) {
                    fail("Text node is not allowed to have children, literal is \"" + ((Text) parent).getLiteral() + "\"");
                }
                boolean lastText = false;
                Node node = parent.getFirstChild();
                while (node != null) {
                    if (node instanceof Text) {
                        if (lastText) {
                            fail("Adjacent text nodes found, second node literal is \"" + ((Text) node).getLiteral() + "\", source:\n" + source);
                        }
                        lastText = true;
                    } else {
                        lastText = false;
                    }
                    node = node.getNext();
                }
                super.visitChildren(parent);
            }
        });
    }

    @Test
    public void testHtmlRendering() {
        assertRendering(example.getSource(), example.getHtml(), render(example.getSource()));
    }

    private String render(String source) {
        return RENDERER.render(PARSER.parse(source));
    }
}
