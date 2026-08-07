package org.commonmark.test;

import org.junit.jupiter.api.Test;

public class ListTightLooseTest extends CoreRenderingTestCase {

    @Test
    public void tight() {
        assertRendering(
                """
                - foo
                - bar
                + baz
                """,
                """
                <ul>
                <li>foo</li>
                <li>bar</li>
                </ul>
                <ul>
                <li>baz</li>
                </ul>
                """);
    }

    @Test
    public void loose() {
        assertRendering(
                """
                - foo

                - bar


                - baz
                """,
                """
                <ul>
                <li>
                <p>foo</p>
                </li>
                <li>
                <p>bar</p>
                </li>
                <li>
                <p>baz</p>
                </li>
                </ul>
                """);
    }

    @Test
    public void looseNested() {
        assertRendering(
                """
                - foo
                  - bar


                    baz
                """,
                """
                <ul>
                <li>foo
                <ul>
                <li>
                <p>bar</p>
                <p>baz</p>
                </li>
                </ul>
                </li>
                </ul>
                """);
    }

    @Test
    public void looseNested2() {
        assertRendering(
                """
                - a
                  - b

                    c
                - d
                """,
                """
                <ul>
                <li>a
                <ul>
                <li>
                <p>b</p>
                <p>c</p>
                </li>
                </ul>
                </li>
                <li>d</li>
                </ul>
                """);
    }

    @Test
    public void looseOuter() {
        assertRendering(
                """
                - foo
                  - bar


                  baz
                """,
                """
                <ul>
                <li>
                <p>foo</p>
                <ul>
                <li>bar</li>
                </ul>
                <p>baz</p>
                </li>
                </ul>
                """);
    }

    @Test
    public void looseListItem() {
        assertRendering(
                """
                - one

                  two
                """,
                """
                <ul>
                <li>
                <p>one</p>
                <p>two</p>
                </li>
                </ul>
                """);
    }

    @Test
    public void tightWithBlankLineAfter() {
        assertRendering(
                """
                - foo
                - bar

                """,
                """
                <ul>
                <li>foo</li>
                <li>bar</li>
                </ul>
                """);
    }

    @Test
    public void tightListWithCodeBlock() {
        assertRendering(
                """
                - a
                - ```
                  b


                  ```
                - c
                """,
                """
                <ul>
                <li>a</li>
                <li>
                <pre><code>b


                </code></pre>
                </li>
                <li>c</li>
                </ul>
                """);
    }

    @Test
    public void tightListWithCodeBlock2() {
        assertRendering(
                """
                * foo
                  ```
                  bar

                  ```
                  baz
                """,
                """
                <ul>
                <li>foo
                <pre><code>bar

                </code></pre>
                baz</li>
                </ul>
                """);
    }

    @Test
    public void looseEmptyListItem() {
        assertRendering(
                """
                * a
                *

                * c""",
                """
                <ul>
                <li>
                <p>a</p>
                </li>
                <li></li>
                <li>
                <p>c</p>
                </li>
                </ul>
                """);
    }

    @Test
    public void looseBlankLineAfterCodeBlock() {
        assertRendering(
                """
                1. ```
                   foo
                   ```

                   bar
                """,
                """
                <ol>
                <li>
                <pre><code>foo
                </code></pre>
                <p>bar</p>
                </li>
                </ol>
                """);
    }
}
