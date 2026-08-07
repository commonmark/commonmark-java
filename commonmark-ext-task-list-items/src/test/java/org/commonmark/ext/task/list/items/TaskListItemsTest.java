package org.commonmark.ext.task.list.items;

import java.util.Set;
import org.commonmark.Extension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.testutil.RenderingTestCase;
import org.junit.jupiter.api.Test;

public class TaskListItemsTest extends RenderingTestCase {

    private static final Set<Extension> EXTENSIONS = Set.of(TaskListItemsExtension.create());
    private static final String HTML_CHECKED =
            "<input type=\"checkbox\" disabled=\"\" checked=\"\">";
    private static final String HTML_UNCHECKED = "<input type=\"checkbox\" disabled=\"\">";
    private static final Parser PARSER = Parser.builder().extensions(EXTENSIONS).build();
    private static final HtmlRenderer RENDERER =
            HtmlRenderer.builder().extensions(EXTENSIONS).build();

    @Test
    public void baseCase() {
        assertRendering(
                """
                - [x] this is *done*
                """,
                """
                <ul>
                <li>%s this is <em>done</em></li>
                </ul>
                """
                        .formatted(HTML_CHECKED));

        assertRendering(
                """
                - [ ] do this
                """,
                """
                <ul>
                <li>%s do this</li>
                </ul>
                """
                        .formatted(HTML_UNCHECKED));

        assertRendering(
                """
                - [x] foo
                  - [ ] bar
                  - [x] baz
                - [ ] bim
                """,
                """
                <ul>
                <li>%s foo
                <ul>
                <li>%s bar</li>
                <li>%s baz</li>
                </ul>
                </li>
                <li>%s bim</li>
                </ul>
                """
                        .formatted(HTML_CHECKED, HTML_UNCHECKED, HTML_CHECKED, HTML_UNCHECKED));

        assertRendering(
                """
                *   [ ]   do this
                *   [ ]   and this
                """,
                """
                <ul>
                <li>%s do this</li>
                <li>%s and this</li>
                </ul>
                """
                        .formatted(HTML_UNCHECKED, HTML_UNCHECKED));

        assertRendering(
                """
                + [x] one
                  - [ ] two
                    * [x] three
                """,
                """
                <ul>
                <li>%s one
                <ul>
                <li>%s two
                <ul>
                <li>%s three</li>
                </ul>
                </li>
                </ul>
                </li>
                </ul>
                """
                        .formatted(HTML_CHECKED, HTML_UNCHECKED, HTML_CHECKED));

        assertRendering(
                """
                TODO list
                ---------
                - [ ] first task
                - [x] second task
                - [ ] third task

                Let me know when you are finished
                """,
                """
                <h2>TODO list</h2>
                <ul>
                <li>%s first task</li>
                <li>%s second task</li>
                <li>%s third task</li>
                </ul>
                <p>Let me know when you are finished</p>
                """
                        .formatted(HTML_UNCHECKED, HTML_CHECKED, HTML_UNCHECKED));
    }

    @Test
    public void notListItem() {
        assertRendering(
                """
                [x] this is not a task
                """,
                """
                <p>[x] this is not a task</p>
                """);
        assertRendering(
                """
                 [ ] this is not a task either
                """,
                """
                <p>[ ] this is not a task either</p>
                """);
    }

    @Test
    public void notValidTaskFormat() {
        assertRendering(
                """
                - [x]no space
                """,
                """
                <ul>
                <li>[x]no space</li>
                </ul>
                """);
        assertRendering(
                """
                - [O] is not a _task_
                """,
                """
                <ul>
                <li>[O] is not a <em>task</em></li>
                </ul>
                """);
        assertRendering(
                """
                * [] neither is this
                """,
                """
                <ul>
                <li>[] neither is this</li>
                </ul>
                """);
        assertRendering(
                """
                * [  ] nor this
                * [XX] nor this
                """,
                """
                <ul>
                <li>[  ] nor this</li>
                <li>[XX] nor this</li>
                </ul>
                """);
        assertRendering(
                """
                + [x]] is not a task
                """,
                """
                <ul>
                <li>[x]] is not a task</li>
                </ul>
                """);
        assertRendering(
                """
                - [x isn't
                """,
                """
                <ul>
                <li>[x isn't</li>
                </ul>
                """);
        assertRendering(
                """
                - [[x is not
                """,
                """
                <ul>
                <li>[[x is not</li>
                </ul>
                """);
        assertRendering(
                """
                - x] nope
                """,
                """
                <ul>
                <li>x] nope</li>
                </ul>
                """);
        assertRendering(
                """
                - x]] no way
                """,
                """
                <ul>
                <li>x]] no way</li>
                </ul>
                """);
        assertRendering(
                """
                + (x) sorry no
                """,
                """
                <ul>
                <li>(x) sorry no</li>
                </ul>
                """);
        assertRendering(
                """
                + {x} sorry not sorry
                """,
                """
                <ul>
                <li>{x} sorry not sorry</li>
                </ul>
                """);
        assertRendering(
                """
                + [[x]] nooo
                """,
                """
                <ul>
                <li>[[x]] nooo</li>
                </ul>
                """);
        assertRendering(
                """
                + text before [x] is not a task
                """,
                """
                <ul>
                <li>text before [x] is not a task</li>
                </ul>
                """);
        assertRendering(
                """
                * [x] \s
                * [ ] \s
                """,
                """
                <ul>
                <li>[x]</li>
                <li>[ ]</li>
                </ul>
                """);
    }

    @Override
    protected String render(String source) {
        return RENDERER.render(PARSER.parse(source));
    }
}
