package org.commonmark.renderer.markdown;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.commonmark.node.Heading;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.NodeRenderer;
import org.junit.Test;

public class NodeRendererFactoriesTest {

    @Test
    public void testAddedNodeRendererFactoryOverrides() {
        String input = "# Header";
        TestCoreMarkdownNodeRenderer.called = false;
        parseAndRender(input, new TestNodeRendererFactory());
        assertTrue(TestCoreMarkdownNodeRenderer.called);
    }

    private void assertRoundTrip(String input, MarkdownNodeRendererFactory factory) {
        String rendered = parseAndRender(input, factory);
        assertEquals(input, rendered);
    }

    private String parseAndRender(String source, MarkdownNodeRendererFactory factory) {
        Node parsed = parse(source);
        return render(parsed, factory);
    }

    private Node parse(String source) {
        return Parser.builder().build().parse(source);
    }

    private String render(Node node, MarkdownNodeRendererFactory factory) {
        return MarkdownRenderer.builder().nodeRendererFactory(factory).build().render(node);
    }

    private static class TestNodeRendererFactory implements MarkdownNodeRendererFactory {
        @Override
        public NodeRenderer create(MarkdownNodeRendererContext context) {
            return new TestCoreMarkdownNodeRenderer(context);
        }

        @Override
        public Set<Character> getSpecialCharacters() {
            return Set.of();
        }
    }

    private static class TestCoreMarkdownNodeRenderer extends CoreMarkdownNodeRenderer {
        static boolean called = false;

        public TestCoreMarkdownNodeRenderer(MarkdownNodeRendererContext context) {
            super(context);
        }

        @Override
        public Set<Class<? extends Node>> getNodeTypes() {
            return super.getNodeTypes();
        }

        @Override
        public void visit(Heading heading) {
            called = true;
            super.visit(heading);
        }
    }

}
