package org.commonmark.test;

import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.html.*;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

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
    @Ignore
    public void parseReaderRender() throws IOException {
        Parser parser = Parser.builder().build();
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream("file.md"), StandardCharsets.UTF_8)) {
            Node document = parser.parseReader(reader);
            // ...
        }
    }

    @Test
    public void visitor() {
        Parser parser = Parser.builder().build();
        Node node = parser.parse("Example\n=======\n\nSome more text");
        WordCountVisitor visitor = new WordCountVisitor();
        node.accept(visitor);
        assertEquals(4, visitor.wordCount);
    }

    @Test
    public void addAttributes() {
        Parser parser = Parser.builder().build();
        HtmlRenderer renderer = HtmlRenderer.builder()
                .attributeProviderFactory(new AttributeProviderFactory() {
                    public AttributeProvider create(AttributeProviderContext context) {
                        return new ImageAttributeProvider();
                    }
                })
                .build();

        Node document = parser.parse("![text](/url.png)");
        assertEquals("<p><img src=\"/url.png\" alt=\"text\" class=\"border\" /></p>\n",
                renderer.render(document));
    }

    @Test
    public void customizeRendering() {
        Parser parser = Parser.builder().build();
        HtmlRenderer renderer = HtmlRenderer.builder()
                .nodeRendererFactory(new HtmlNodeRendererFactory() {
                    public NodeRenderer create(HtmlNodeRendererContext context) {
                        return new IndentedCodeBlockNodeRenderer(context);
                    }
                })
                .build();

        Node document = parser.parse("Example:\n\n    code");
        assertEquals("<p>Example:</p>\n<pre>code\n</pre>\n", renderer.render(document));
    }

    class WordCountVisitor extends AbstractVisitor {

        int wordCount = 0;

        @Override
        public void visit(Text text) {
            // This is called for all Text nodes. Override other visit methods for other node types.

            // Count words (this is just an example, don't actually do it this way for various reasons).
            wordCount += text.getLiteral().split("\\W+").length;

            // Descend into children (could be omitted in this case because Text nodes don't have children).
            visitChildren(text);
        }
    }

    class ImageAttributeProvider implements AttributeProvider {
        @Override
        public void setAttributes(Node node, String tagName, Map<String, String> attributes) {
            if (node instanceof Image) {
                attributes.put("class", "border");
            }
        }
    }

    class IndentedCodeBlockNodeRenderer implements NodeRenderer {

        private final HtmlWriter html;

        IndentedCodeBlockNodeRenderer(HtmlNodeRendererContext context) {
            this.html = context.getWriter();
        }

        @Override
        public Set<Class<? extends Node>> getNodeTypes() {
            // Return the node types we want to use this renderer for.
            return Collections.<Class<? extends Node>>singleton(IndentedCodeBlock.class);
        }

        @Override
        public void render(Node node) {
            // We only handle one type as per getNodeTypes, so we can just cast it here.
            IndentedCodeBlock codeBlock = (IndentedCodeBlock) node;
            html.line();
            html.tag("pre");
            html.text(codeBlock.getLiteral());
            html.tag("/pre");
            html.line();
        }
    }
}
