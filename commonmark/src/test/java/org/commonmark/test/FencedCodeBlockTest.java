package org.commonmark.test;

import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.testutil.RenderingTestCase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FencedCodeBlockTest extends RenderingTestCase {

    private static final Parser PARSER = Parser.builder().build();
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder().build();

    @Test
    public void backtickInfo() {
        Node document = PARSER.parse("```info ~ test\ncode\n```");
        FencedCodeBlock codeBlock = (FencedCodeBlock) document.getFirstChild();
        assertEquals("info ~ test", codeBlock.getInfo());
        assertEquals("code\n", codeBlock.getLiteral());
    }

    @Test
    public void backtickInfoDoesntAllowBacktick() {
        assertRendering("```info ` test\ncode\n```",
                "<p>```info ` test\ncode</p>\n<pre><code></code></pre>\n");
        // Note, it's unclear in the spec whether a ~~~ code block can contain ` in info or not, see:
        // https://github.com/commonmark/CommonMark/issues/119
    }

    @Test
    public void backtickAndTildeCantBeMixed() {
        assertRendering("``~`\ncode\n``~`",
                "<p><code>~` code</code>~`</p>\n");
    }

    @Test
    public void closingCanHaveSpacesAfter() {
        assertRendering("```\ncode\n```   ",
                "<pre><code>code\n</code></pre>\n");
    }

    @Test
    public void closingCanNotHaveNonSpaces() {
        assertRendering("```\ncode\n``` a",
                "<pre><code>code\n``` a\n</code></pre>\n");
    }

    @Override
    protected String render(String source) {
        return RENDERER.render(PARSER.parse(source));
    }
}
