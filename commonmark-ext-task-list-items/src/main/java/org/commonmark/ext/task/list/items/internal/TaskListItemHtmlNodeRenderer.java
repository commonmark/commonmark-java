package org.commonmark.ext.task.list.items.internal;

import org.commonmark.ext.task.list.items.TaskListItemMarker;
import org.commonmark.node.Node;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.renderer.html.HtmlWriter;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class TaskListItemHtmlNodeRenderer implements NodeRenderer {

    private final HtmlNodeRendererContext context;
    private final HtmlWriter html;

    public TaskListItemHtmlNodeRenderer(HtmlNodeRendererContext context) {
        this.context = context;
        this.html = context.getWriter();
    }

    @Override
    public Set<Class<? extends Node>> getNodeTypes() {
        return Collections.<Class<? extends Node>>singleton(TaskListItemMarker.class);
    }

    @Override
    public void render(Node node) {
        if (node instanceof TaskListItemMarker) {
            Map<String, String> attributes = context.extendAttributes(node, "input", Collections.<String, String>emptyMap());
            attributes.put("type", "checkbox");
            attributes.put("disabled", "");
            if (((TaskListItemMarker) node).isChecked()) {
                attributes.put("checked", "");
            }
            html.tag("input", attributes);
            // Add a space after the input tag (as the next text node has been trimmed)
            html.text(" ");
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
