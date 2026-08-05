package org.commonmark.test;

import org.junit.jupiter.api.Test;

public class HtmlInlineParserTest extends CoreRenderingTestCase {

    @Test
    public void comment() {
        assertRendering("inline <!---->", "<p>inline <!----></p>\n");
        assertRendering("inline <!-- -> -->", "<p>inline <!-- -> --></p>\n");
        assertRendering("inline <!-- -- -->", "<p>inline <!-- -- --></p>\n");
        assertRendering("inline <!-- --->", "<p>inline <!-- ---></p>\n");
        assertRendering("inline <!-- ---->", "<p>inline <!-- ----></p>\n");
        assertRendering("inline <!-->-->", "<p>inline <!-->--&gt;</p>\n");
        assertRendering("inline <!--->-->", "<p>inline <!--->--&gt;</p>\n");
    }

    @Test
    public void cdata() {
        assertRendering("inline <![CDATA[]]>", "<p>inline <![CDATA[]]></p>\n");
        assertRendering("inline <![CDATA[ ] ]] ]]>", "<p>inline <![CDATA[ ] ]] ]]></p>\n");
    }

    @Test
    public void afterFailedAttempt() {
        // A `<` that doesn't start inline HTML must not stop a later one from being parsed
        assertRendering("inline <!- <!-- a -->", "<p>inline &lt;!- <!-- a --></p>\n");
        assertRendering("inline <?? <?php ?>", "<p>inline &lt;?? <?php ?></p>\n");
        assertRendering(
                "inline <![x]]> <![CDATA[a]]>", "<p>inline &lt;![x]]&gt; <![CDATA[a]]></p>\n");
        assertRendering("inline <!foo> <!bar baz>", "<p>inline &lt;!foo&gt; <!bar baz></p>\n");
    }

    @Test
    public void unterminatedConstructDoesNotAffectLaterParagraph() {
        // A scan that reaches the end of the input only says something about the inline snippet it
        // ran in, and a parser is created for each of those
        assertRendering("x <?a\n\nx <?b?>", "<p>x &lt;?a</p>\n<p>x <?b?></p>\n");
        assertRendering("x <!--a\n\nx <!--b-->", "<p>x &lt;!--a</p>\n<p>x <!--b--></p>\n");
        assertRendering(
                "x <![CDATA[a\n\nx <![CDATA[b]]>",
                "<p>x &lt;![CDATA[a</p>\n<p>x <![CDATA[b]]></p>\n");
        assertRendering("x <!A a\n\nx <!A b>", "<p>x &lt;!A a</p>\n<p>x <!A b></p>\n");
    }

    @Test
    public void declaration() {
        // Whitespace is mandatory
        assertRendering("inline <!FOO>", "<p>inline &lt;!FOO&gt;</p>\n");
        assertRendering("inline <!FOO >", "<p>inline <!FOO ></p>\n");
        assertRendering("inline <!FOO 'bar'>", "<p>inline <!FOO 'bar'></p>\n");

        // Lowercase
        assertRendering("inline <!foo bar>", "<p>inline <!foo bar></p>\n");
    }
}
