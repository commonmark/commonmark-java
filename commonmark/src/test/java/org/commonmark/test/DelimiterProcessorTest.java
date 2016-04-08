package org.commonmark.test;

import org.commonmark.html.HtmlRenderer;
import org.commonmark.html.renderer.NodeRenderer;
import org.commonmark.html.renderer.NodeRendererContext;
import org.commonmark.html.renderer.NodeRendererFactory;
import org.commonmark.node.CustomNode;
import org.commonmark.node.Node;
import org.commonmark.node.Text;
import org.commonmark.parser.DelimiterProcessor;
import org.commonmark.parser.Parser;
import org.junit.Test;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;

public class DelimiterProcessorTest extends RenderingTestCase {

    private static final Parser PARSER = Parser.builder().customDelimiterProcessor(new AsymmetricDelimiterProcessor()).build();
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder().nodeRendererFactory(new UpperCaseNodeRendererFactory()).build();

    @Test
    public void asymmetricDelimiter() {
        assertRendering("{foo} bar", "<p>FOO bar</p>\n");
        assertRendering("f{oo ba}r", "<p>fOO BAr</p>\n");
        assertRendering("{{foo} bar", "<p>{FOO bar</p>\n");
        assertRendering("{foo}} bar", "<p>FOO} bar</p>\n");
        assertRendering("{{foo} bar}", "<p>FOO BAR</p>\n");
        assertRendering("{foo bar", "<p>{foo bar</p>\n");
        assertRendering("foo} bar", "<p>foo} bar</p>\n");
        assertRendering("}foo} bar", "<p>}foo} bar</p>\n");
        assertRendering("{foo{ bar", "<p>{foo{ bar</p>\n");
        assertRendering("}foo{ bar", "<p>}foo{ bar</p>\n");
    }

    @Override
    protected String render(String source) {
        Node node = PARSER.parse(source);
        return RENDERER.render(node);
    }

    private static class AsymmetricDelimiterProcessor implements DelimiterProcessor {

        @Override
        public char getOpeningDelimiterChar() {
            return '{';
        }

        @Override
        public char getClosingDelimiterChar() {
            return '}';
        }

        @Override
        public int getMinDelimiterCount() {
            return 1;
        }

        @Override
        public int getDelimiterUse(int openerCount, int closerCount) {
            return 1;
        }

        @Override
        public void process(Text opener, Text closer, int delimiterUse) {
            UpperCaseNode content = new UpperCaseNode();
            Node tmp = opener.getNext();
            while (tmp != null && tmp != closer) {
                Node next = tmp.getNext();
                content.appendChild(tmp);
                tmp = next;
            }
            opener.insertAfter(content);
        }
    }

    private static class UpperCaseNode extends CustomNode {
    }

    private static class UpperCaseNodeRendererFactory implements NodeRendererFactory {

        @Override
        public NodeRenderer create(NodeRendererContext context) {
            return new UpperCaseNodeRenderer(context);
        }
    }

    private static class UpperCaseNodeRenderer implements NodeRenderer {

        private final NodeRendererContext context;

        private UpperCaseNodeRenderer(NodeRendererContext context) {
            this.context = context;
        }

        @Override
        public Set<Class<? extends Node>> getNodeTypes() {
            return Collections.<Class<? extends Node>>singleton(UpperCaseNode.class);
        }

        @Override
        public void render(Node node) {
            UpperCaseNode upperCaseNode = (UpperCaseNode) node;
            for (Node child = upperCaseNode.getFirstChild(); child != null; child = child.getNext()) {
                if (child instanceof Text) {
                    Text text = (Text) child;
                    text.setLiteral(text.getLiteral().toUpperCase(Locale.ENGLISH));
                }
                context.render(child);
            }
        }
    }
}
