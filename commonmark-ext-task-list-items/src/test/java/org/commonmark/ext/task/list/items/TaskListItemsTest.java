package org.commonmark.ext.task.list.items;

import org.commonmark.Extension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.testutil.RenderingTestCase;
import org.junit.Test;

import java.util.Set;

public class TaskListItemsTest extends RenderingTestCase {

    private static final Set<Extension> EXTENSIONS = Set.of(TaskListItemsExtension.create());
    private static final String HTML_CHECKED = "<input type=\"checkbox\" disabled=\"\" checked=\"\">";
    private static final String HTML_UNCHECKED = "<input type=\"checkbox\" disabled=\"\">";
    private static final Parser PARSER = Parser.builder().extensions(EXTENSIONS).build();
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder().extensions(EXTENSIONS).build();

    @Test
    public void baseCase() {
        assertRendering("- [x] this is *done*\n", "<ul>\n<li>" + HTML_CHECKED + " this is <em>done</em></li>\n</ul>\n");

        assertRendering("- [ ] do this\n", "<ul>\n<li>" + HTML_UNCHECKED + " do this</li>\n</ul>\n");

        assertRendering("- [x] foo\n" +
                        "  - [ ] bar\n" +
                        "  - [x] baz\n" +
                        "- [ ] bim",
                "<ul>\n" +
                "<li>" + HTML_CHECKED + " foo\n" +
                "<ul>\n" +
                "<li>" + HTML_UNCHECKED + " bar</li>\n" +
                "<li>" + HTML_CHECKED + " baz</li>\n" +
                "</ul>\n" +
                "</li>\n" +
                "<li>" + HTML_UNCHECKED + " bim</li>\n" +
                "</ul>\n");

        assertRendering("*   [ ]   do this\n*   [ ]   and this",
                "<ul>\n<li>" + HTML_UNCHECKED + " do this</li>\n<li>" + HTML_UNCHECKED + " and this</li>\n</ul>\n");

        assertRendering("+ [x] one\n" +
                        "  - [ ] two\n" +
                        "    * [x] three\n",
                "<ul>\n" +
                "<li>" + HTML_CHECKED + " one\n" +
                "<ul>\n" +
                "<li>" + HTML_UNCHECKED + " two\n" +
                "<ul>\n" +
                "<li>" + HTML_CHECKED + " three</li>\n" +
                "</ul>\n" +
                "</li>\n" +
                "</ul>\n" +
                "</li>\n" +
                "</ul>\n");

        assertRendering("TODO list\n" +
                        "---------\n" +
                        "- [ ] first task\n" +
                        "- [x] second task\n" +
                        "- [ ] third task\n\n" +
                        "Let me know when you are finished",
                "<h2>TODO list</h2>\n" +
                "<ul>\n" +
                "<li>" + HTML_UNCHECKED + " first task</li>\n" +
                "<li>" + HTML_CHECKED + " second task</li>\n" +
                "<li>" + HTML_UNCHECKED + " third task</li>\n" +
                "</ul>\n" +
                "<p>Let me know when you are finished</p>\n");
    }

    @Test
    public void notListItem() {
        assertRendering("[x] this is not a task\n", "<p>[x] this is not a task</p>\n");
        assertRendering(" [ ] this is not a task either\n", "<p>[ ] this is not a task either</p>\n");
    }

    @Test
    public void notValidTaskFormat() {
        assertRendering("- [x]no space\n", "<ul>\n<li>[x]no space</li>\n</ul>\n");
        assertRendering("- [O] is not a _task_\n", "<ul>\n<li>[O] is not a <em>task</em></li>\n</ul>\n");
        assertRendering("* [] neither is this\n", "<ul>\n<li>[] neither is this</li>\n</ul>\n");
        assertRendering("* [  ] nor this\n" +
                        "* [XX] nor this\n",
                "<ul>\n<li>[  ] nor this</li>\n<li>[XX] nor this</li>\n</ul>\n");
        assertRendering("+ [x]] is not a task\n", "<ul>\n<li>[x]] is not a task</li>\n</ul>\n");
        assertRendering("- [x isn't\n", "<ul>\n<li>[x isn't</li>\n</ul>\n");
        assertRendering("- [[x is not\n", "<ul>\n<li>[[x is not</li>\n</ul>\n");
        assertRendering("- x] nope\n", "<ul>\n<li>x] nope</li>\n</ul>\n");
        assertRendering("- x]] no way\n", "<ul>\n<li>x]] no way</li>\n</ul>\n");
        assertRendering("+ (x) sorry no\n", "<ul>\n<li>(x) sorry no</li>\n</ul>\n");
        assertRendering("+ {x} sorry not sorry\n", "<ul>\n<li>{x} sorry not sorry</li>\n</ul>\n");
        assertRendering("+ [[x]] nooo\n", "<ul>\n<li>[[x]] nooo</li>\n</ul>\n");
        assertRendering("+ text before [x] is not a task\n", "<ul>\n<li>text before [x] is not a task</li>\n</ul>\n");
        assertRendering("* [x]  \n* [ ]  \n", "<ul>\n<li>[x]</li>\n<li>[ ]</li>\n</ul>\n");
    }

    @Override
    protected String render(String source) {
        return RENDERER.render(PARSER.parse(source));
    }
}
