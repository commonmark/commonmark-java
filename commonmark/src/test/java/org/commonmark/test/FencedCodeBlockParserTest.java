package org.commonmark.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.commonmark.node.FencedCodeBlock;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.testutil.RenderingTestCase;
import org.junit.jupiter.api.Test;

public class FencedCodeBlockParserTest extends RenderingTestCase {

    private static final Parser PARSER = Parser.builder().build();
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder().build();

    @Test
    public void backtickInfo() {
        var s =
                """
                ```info ~ test
                code
                ```
                """;
        var document = PARSER.parse(s);
        FencedCodeBlock codeBlock = (FencedCodeBlock) document.getFirstChild();
        assertThat(codeBlock.getInfo()).isEqualTo("info ~ test");
        assertThat(codeBlock.getLiteral()).isEqualTo("code\n");
    }

    @Test
    public void backtickInfoDoesntAllowBacktick() {
        assertRendering(
                """
                ```info ` test
                code
                ```
                """,
                """
                <p>```info ` test
                code</p>
                <pre><code></code></pre>
                """);
    }

    @Test
    public void backtickAndTildeCantBeMixed() {
        assertRendering(
                """
                ``~`
                code
                ``~`
                """,
                """
                <p><code>~` code </code>~`</p>
                """);
    }

    @Test
    public void closingCanHaveSpacesAfter() {
        assertRendering(
                """
                ```
                code
                ```  \s
                """,
                """
                <pre><code>code
                </code></pre>
                """);
    }

    @Test
    public void closingCanNotHaveNonSpaces() {
        assertRendering(
                """
                ```
                code
                ``` a
                """,
                """
                <pre><code>code
                ``` a
                </code></pre>
                """);
    }

    @Test
    public void issue151() {
        assertRendering(
                """
                ```
                this code

                should not have BRs or paragraphs in it
                ok
                ```
                """,
                """
                <pre><code>this code

                should not have BRs or paragraphs in it
                ok
                </code></pre>
                """);
    }

    @Override
    protected String render(String source) {
        return RENDERER.render(PARSER.parse(source));
    }
}
