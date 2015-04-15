package org.commonmark.test;

import org.commonmark.html.CodeBlockAttributeProvider;
import org.commonmark.html.HtmlRenderer;
import org.commonmark.Parser;
import org.commonmark.node.CodeBlock;
import org.commonmark.node.Node;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class HtmlRendererTest {

    @Test
    public void htmlAllowingShouldNotEscapeInlineHtml() {
        String rendered = htmlAllowingRenderer().render(parse("paragraph with <span id='foo' class=\"bar\">inline &amp; html</span>"));
        assertEquals("<p>paragraph with <span id='foo' class=\"bar\">inline &amp; html</span></p>\n", rendered);
    }

    @Test
    public void htmlAllowingShouldNotEscapeBlockHtml() {
        String rendered = htmlAllowingRenderer().render(parse("<div id='foo' class=\"bar\">block &amp;</div>"));
        assertEquals("<div id='foo' class=\"bar\">block &amp;</div>\n", rendered);
    }

    @Test
    public void htmlEscapingShouldEscapeInlineHtml() {
        String rendered = htmlEscapingRenderer().render(parse("paragraph with <span id='foo' class=\"bar\">inline &amp; html</span>"));
        // Note that &amp; is not escaped, as it's a normal text node, not part of the inline HTML.
        assertEquals("<p>paragraph with &lt;span id='foo' class=&quot;bar&quot;&gt;inline &amp; html&lt;/span&gt;</p>\n", rendered);
    }

    @Test
    public void htmlEscapingShouldEscapeHtmlBlocks() {
        String rendered = htmlEscapingRenderer().render(parse("<div id='foo' class=\"bar\">block &amp;</div>"));
        assertEquals("&lt;div id='foo' class=&quot;bar&quot;&gt;block &amp;amp;&lt;/div&gt;\n", rendered);
    }

    @Test
    public void textEscaping() {
        String rendered = defaultRenderer().render(parse("escaping: & < > \" '"));
        assertEquals("<p>escaping: &amp; &lt; &gt; &quot; '</p>\n", rendered);
    }

    @Test
    public void codeBlockAttributeProvider() {
        CodeBlockAttributeProvider custom = new CodeBlockAttributeProvider() {
            @Override
            public Map<String, String> getAttributes(CodeBlock codeBlock) {
                Map<String, String> attributes = new HashMap<>();
                attributes.put("data-custom", codeBlock.getInfo());
                return attributes;
            }
        };

        HtmlRenderer renderer = HtmlRenderer.builder().codeBlockAttributeProvider(custom).build();
        String rendered = renderer.render(parse("```info\ncontent\n```"));
        assertEquals("<pre><code data-custom=\"info\">content\n</code></pre>\n", rendered);

        String rendered2 = renderer.render(parse("```evil\"\ncontent\n```"));
        assertEquals("<pre><code data-custom=\"evil&quot;\">content\n</code></pre>\n", rendered2);
    }

    private static HtmlRenderer defaultRenderer() {
        return HtmlRenderer.builder().build();
    }

    private static HtmlRenderer htmlAllowingRenderer() {
        return HtmlRenderer.builder().escapeHtml(false).build();
    }

    private static HtmlRenderer htmlEscapingRenderer() {
        return HtmlRenderer.builder().escapeHtml(true).build();
    }

    private static Node parse(String source) {
        return Parser.builder().build().parse(source);
    }
}
