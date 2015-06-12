package org.commonmark.test;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Node;
import org.commonmark.node.Text;
import org.commonmark.spec.SpecExample;
import org.commonmark.spec.SpecReader;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class SpecTest extends RenderingTestCase {

    protected final SpecExample example;

    public SpecTest(SpecExample example) {
        this.example = example;
    }

    @Parameters(name = "{0}")
    public static List<Object[]> data() {
        List<SpecExample> examples = SpecReader.readExamples();
        List<Object[]> data = new ArrayList<>();
        for (SpecExample example : examples) {
            data.add(new Object[]{example});
        }
        return data;
    }

    @Test
    public void testHtmlRendering() {
        assertRendering(example.getSource(), example.getHtml());
    }

    @Test
    public void testTextNodesContiguous() {
        final String source = example.getSource();
        Node node = parser.parse(source);
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

}
