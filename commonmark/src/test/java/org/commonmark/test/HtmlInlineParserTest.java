package org.commonmark.test;

import org.junit.Test;

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
    public void declaration() {
        // Whitespace is mandatory
        assertRendering("inline <!FOO>", "<p>inline &lt;!FOO&gt;</p>\n");
        assertRendering("inline <!FOO >", "<p>inline <!FOO ></p>\n");
        assertRendering("inline <!FOO 'bar'>", "<p>inline <!FOO 'bar'></p>\n");

        // Lowercase
        assertRendering("inline <!foo bar>", "<p>inline <!foo bar></p>\n");
    }
}
