package org.commonmark.ext.task.list.items.internal;

import org.commonmark.ext.task.list.items.TaskListItemMarker;
import org.commonmark.node.Node;
import org.commonmark.renderer.markdown.MarkdownNodeRendererContext;
import org.commonmark.renderer.markdown.MarkdownWriter;

public class TaskListItemMarkdownNodeRenderer extends TaskListItemNodeRenderer {

    private final MarkdownNodeRendererContext context;
    private final MarkdownWriter writer;

    public TaskListItemMarkdownNodeRenderer(MarkdownNodeRendererContext context) {
        this.context = context;
        this.writer = context.getWriter();
    }

    @Override
    public void render(Node node) {
        if (node instanceof TaskListItemMarker) {
            TaskListItemMarker taskListItemNode = (TaskListItemMarker) node;
            var checkboxFill = taskListItemNode.isChecked() ? "x" : " ";
            writer.raw("[" + checkboxFill + "] ");
            renderChildren(node);
        }
    }

    private void renderChildren(Node parent) {
        Node node = parent.getFirstChild();
        while (node != null) {
            Node next = node.getNext();
            context.render(node);
            node = next;
        }
    }
}
