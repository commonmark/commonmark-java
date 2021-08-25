package org.commonmark.test;

import org.commonmark.node.CustomNode;
import org.commonmark.node.Node;
import org.commonmark.node.Text;
import org.commonmark.parser.Parser;
import org.commonmark.parser.delimiter.DelimiterProcessor;
import org.commonmark.parser.delimiter.DelimiterRun;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.renderer.html.HtmlNodeRendererFactory;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.testutil.RenderingTestCase;
import org.junit.Test;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class DelimiterProcessorTest extends RenderingTestCase {

    private static final Parser PARSER = Parser.builder().customDelimiterProcessor(new AsymmetricDelimiterProcessor()).build();
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder().nodeRendererFactory(new UpperCaseNodeRendererFactory()).build();

    @Test
    public void delimiterProcessorWithInvalidDelimiterUse() {
        Parser parser = Parser.builder()
                .customDelimiterProcessor(new CustomDelimiterProcessor(':', 0))
                .customDelimiterProcessor(new CustomDelimiterProcessor(';', -1))
                .build();
        assertEquals("<p>:test:</p>\n", RENDERER.render(parser.parse(":test:")));
        assertEquals("<p>;test;</p>\n", RENDERER.render(parser.parse(";test;")));
    }

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
        assertRendering("{} {foo}", "<p> FOO</p>\n");
    }

    @Test
    public void multipleDelimitersWithDifferentLengths() {
        Parser parser = Parser.builder()
                .customDelimiterProcessor(new OneDelimiterProcessor())
                .customDelimiterProcessor(new TwoDelimiterProcessor())
                .build();
        assertEquals("<p>(1)one(/1) (2)two(/2)</p>\n", RENDERER.render(parser.parse("+one+ ++two++")));
        assertEquals("<p>(1)(2)both(/2)(/1)</p>\n", RENDERER.render(parser.parse("+++both+++")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void multipleDelimitersWithSameLength() {
        Parser.builder()
                .customDelimiterProcessor(new OneDelimiterProcessor())
                .customDelimiterProcessor(new OneDelimiterProcessor())
                .build();
    }

    @Override
    protected String render(String source) {
        Node node = PARSER.parse(source);
        return RENDERER.render(node);
    }

    private static class CustomDelimiterProcessor implements DelimiterProcessor {

        private final char delimiterChar;
        private final int delimiterUse;

        private CustomDelimiterProcessor(char delimiterChar, int delimiterUse) {
            this.delimiterChar = delimiterChar;
            this.delimiterUse = delimiterUse;
        }

        @Override
        public char getOpeningCharacter() {
            return delimiterChar;
        }

        @Override
        public char getClosingCharacter() {
            return delimiterChar;
        }

        @Override
        public int getMinLength() {
            return 1;
        }

        @Override
        public int process(DelimiterRun openingRun, DelimiterRun closingRun) {
            return delimiterUse;
        }

        @Override
        public int process(DelimiterRun openingRun, DelimiterRun closingRun, String prefix) {
            return process(openingRun, closingRun);
        }
    }

    private static class AsymmetricDelimiterProcessor implements DelimiterProcessor {

        @Override
        public char getOpeningCharacter() {
            return '{';
        }

        @Override
        public char getClosingCharacter() {
            return '}';
        }

        @Override
        public int getMinLength() {
            return 1;
        }

        @Override
        public int process(DelimiterRun openingRun, DelimiterRun closingRun) {
            UpperCaseNode content = new UpperCaseNode();
            Text start = openingRun.getOpener();
            Text end = closingRun.getCloser();
            Node tmp = start.getNext();
            while (tmp != null && tmp != end) {
                Node next = tmp.getNext();
                content.appendChild(tmp);
                tmp = next;
            }
            start.insertAfter(content);

            return 1;
        }

        @Override
        public int process(DelimiterRun openingRun, DelimiterRun closingRun, String prefix) {
            return process(openingRun, closingRun);
        }
    }

    private static class UpperCaseNode extends CustomNode {
    }

    private static class UpperCaseNodeRendererFactory implements HtmlNodeRendererFactory {

        @Override
        public NodeRenderer create(HtmlNodeRendererContext context) {
            return new UpperCaseNodeRenderer(context);
        }
    }

    private static class UpperCaseNodeRenderer implements NodeRenderer {

        private final HtmlNodeRendererContext context;

        private UpperCaseNodeRenderer(HtmlNodeRendererContext context) {
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

    private static class OneDelimiterProcessor implements DelimiterProcessor {

        @Override
        public char getOpeningCharacter() {
            return '+';
        }

        @Override
        public char getClosingCharacter() {
            return '+';
        }

        @Override
        public int getMinLength() {
            return 1;
        }

        @Override
        public int process(DelimiterRun openingRun, DelimiterRun closingRun) {
            openingRun.getOpener().insertAfter(new Text("(1)"));
            closingRun.getCloser().insertBefore(new Text("(/1)"));
            return 1;
        }

        @Override
        public int process(DelimiterRun openingRun, DelimiterRun closingRun, String prefix) {
            return process(openingRun, closingRun);
        }
    }

    private static class TwoDelimiterProcessor implements DelimiterProcessor {

        @Override
        public char getOpeningCharacter() {
            return '+';
        }

        @Override
        public char getClosingCharacter() {
            return '+';
        }

        @Override
        public int getMinLength() {
            return 2;
        }

        @Override
        public int process(DelimiterRun openingRun, DelimiterRun closingRun) {
            openingRun.getOpener().insertAfter(new Text("(2)"));
            closingRun.getCloser().insertBefore(new Text("(/2)"));
            return 2;
        }

        @Override
        public int process(DelimiterRun openingRun, DelimiterRun closingRun, String prefix) {
            return process(openingRun, closingRun);
        }
    }
}
