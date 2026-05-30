package org.commonmark.ext.task.list.items;

import java.util.Set;
import org.commonmark.Extension;
import org.commonmark.node.BulletList;
import org.commonmark.node.Document;
import org.commonmark.node.ListItem;
import org.commonmark.node.Node;
import org.commonmark.node.Paragraph;
import org.commonmark.node.Text;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.markdown.MarkdownRenderer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TaskListItemMarkdownRendererTest {

    private static final Set<Extension> EXTENSIONS = Set.of(TaskListItemsExtension.create());
    private static final Parser PARSER = Parser.builder().extensions(EXTENSIONS).build();
    private static final MarkdownRenderer RENDERER = MarkdownRenderer.builder().extensions(EXTENSIONS).build();

    @Test
    public void testCheckedRoundTrip() {
        assertRoundTrip("- [x] I am checked\n");
    }

    @Test
    public void testUncheckedRoundTrip() {
        assertRoundTrip("- [ ] I am unchecked\n");
    }

    @Test
    public void testMixedRoundTrip() {
        assertRoundTrip("- [x] I am checked\n- [ ] I am unchecked\n");
    }

    @Test
    public void testNestedRoundTrip() {
        assertRoundTrip("- [ ] I am unchecked\n  - [x] I am a checked child\n");
    }

    @Test
    public void testFormattingRoundTrip() {
        assertRoundTrip("- [x] I am **boldly** checked\n- [ ] I am *italicly* unchecked\n");
    }

    @Test
    public void testNonTaskListItemRoundTrip() {
        assertRoundTrip("- [x] I am checked\n- [ ] I am unchecked\n- I am not a task item\n");
    }

    @Test
    public void testOrderedListRoundTrip() {
        assertRoundTrip("1. [x] I am checked\n2. [ ] I am unchecked\n");
    }

    @Test
    public void testProgrammaticallyBuilt() {
        var doc = new Document();
        var list = new BulletList();
        var item = new ListItem();
        var taskMarker = new TaskListItemMarker(false);
        var para = new Paragraph();
        var text = new Text("I am a task");
        para.appendChild(text);
        item.appendChild(taskMarker);
        item.appendChild(para);
        list.appendChild(item);
        doc.appendChild(list);

        assertRenderedEquals(doc, "- [ ] I am a task\n");
    }

    private void assertRoundTrip(String input) {
        String rendered = RENDERER.render(PARSER.parse(input));
        assertThat(rendered).isEqualTo(input);
    }

    private void assertRenderedEquals(Node inputNode, String expectedOutput) {
        var renderedOutput = RENDERER.render(inputNode);
        assertThat(renderedOutput).isEqualTo(expectedOutput);
    }
}
