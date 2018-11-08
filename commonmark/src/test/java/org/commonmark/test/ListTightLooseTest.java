package org.commonmark.test;

import org.junit.Test;

public class ListTightLooseTest extends CoreRenderingTestCase {

    @Test
    public void tight() {
        assertRendering("- foo\n" +
                        "- bar\n" +
                        "+ baz\n",
                "<ul>\n" +
                        "<li>foo</li>\n" +
                        "<li>bar</li>\n" +
                        "</ul>\n" +
                        "<ul>\n" +
                        "<li>baz</li>\n" +
                        "</ul>\n");
    }

    @Test
    public void loose() {
        assertRendering("- foo\n" +
                        "\n" +
                        "- bar\n" +
                        "\n" +
                        "\n" +
                        "- baz\n",
                "<ul>\n" +
                        "<li>\n" +
                        "<p>foo</p>\n" +
                        "</li>\n" +
                        "<li>\n" +
                        "<p>bar</p>\n" +
                        "</li>\n" +
                        "<li>\n" +
                        "<p>baz</p>\n" +
                        "</li>\n" +
                        "</ul>\n");
    }

    @Test
    public void looseNested() {
        assertRendering("- foo\n" +
                        "  - bar\n" +
                        "\n" +
                        "\n" +
                        "    baz",
                "<ul>\n" +
                        "<li>foo\n" +
                        "<ul>\n" +
                        "<li>\n" +
                        "<p>bar</p>\n" +
                        "<p>baz</p>\n" +
                        "</li>\n" +
                        "</ul>\n" +
                        "</li>\n" +
                        "</ul>\n");
    }

    @Test
    public void looseNested2() {
        assertRendering("- a\n" +
                        "  - b\n" +
                        "\n" +
                        "    c\n" +
                        "- d\n",
                "<ul>\n" +
                        "<li>a\n" +
                        "<ul>\n" +
                        "<li>\n" +
                        "<p>b</p>\n" +
                        "<p>c</p>\n" +
                        "</li>\n" +
                        "</ul>\n" +
                        "</li>\n" +
                        "<li>d</li>\n" +
                        "</ul>\n");
    }

    @Test
    public void looseOuter() {
        assertRendering("- foo\n" +
                        "  - bar\n" +
                        "\n" +
                        "\n" +
                        "  baz",
                "<ul>\n" +
                        "<li>\n" +
                        "<p>foo</p>\n" +
                        "<ul>\n" +
                        "<li>bar</li>\n" +
                        "</ul>\n" +
                        "<p>baz</p>\n" +
                        "</li>\n" +
                        "</ul>\n");
    }

    @Test
    public void looseListItem() {
        assertRendering("- one\n" +
                        "\n" +
                        "  two\n",
                "<ul>\n" +
                        "<li>\n" +
                        "<p>one</p>\n" +
                        "<p>two</p>\n" +
                        "</li>\n" +
                        "</ul>\n");
    }

    @Test
    public void tightWithBlankLineAfter() {
        assertRendering("- foo\n" +
                        "- bar\n" +
                        "\n",
                "<ul>\n" +
                        "<li>foo</li>\n" +
                        "<li>bar</li>\n" +
                        "</ul>\n");
    }

    @Test
    public void tightListWithCodeBlock() {
        assertRendering("- a\n" +
                        "- ```\n" +
                        "  b\n" +
                        "\n" +
                        "\n" +
                        "  ```\n" +
                        "- c\n",
                "<ul>\n" +
                        "<li>a</li>\n" +
                        "<li>\n" +
                        "<pre><code>b\n" +
                        "\n" +
                        "\n" +
                        "</code></pre>\n" +
                        "</li>\n" +
                        "<li>c</li>\n" +
                        "</ul>\n");
    }

    @Test
    public void tightListWithCodeBlock2() {
        assertRendering("* foo\n" +
                        "  ```\n" +
                        "  bar\n" +
                        "\n" +
                        "  ```\n" +
                        "  baz\n",
                "<ul>\n" +
                        "<li>foo\n" +
                        "<pre><code>bar\n" +
                        "\n" +
                        "</code></pre>\n" +
                        "baz</li>\n" +
                        "</ul>\n");
    }

    @Test
    public void looseEmptyListItem() {
        assertRendering("* a\n" +
                        "*\n" +
                        "\n" +
                        "* c",
                "<ul>\n" +
                        "<li>\n" +
                        "<p>a</p>\n" +
                        "</li>\n" +
                        "<li></li>\n" +
                        "<li>\n" +
                        "<p>c</p>\n" +
                        "</li>\n" +
                        "</ul>\n");
    }

    @Test
    public void looseBlankLineAfterCodeBlock() {
        assertRendering("1. ```\n" +
                        "   foo\n" +
                        "   ```\n" +
                        "\n" +
                        "   bar",
                "<ol>\n" +
                        "<li>\n" +
                        "<pre><code>foo\n" +
                        "</code></pre>\n" +
                        "<p>bar</p>\n" +
                        "</li>\n" +
                        "</ol>\n");
    }
}
