package org.commonmark.ext.gfm.alerts.internal;

import org.commonmark.ext.gfm.alerts.Alert;
import org.commonmark.ext.gfm.alerts.AlertTitle;
import org.commonmark.node.Node;
import org.commonmark.renderer.markdown.MarkdownNodeRendererContext;
import org.commonmark.renderer.markdown.MarkdownWriter;

public class AlertMarkdownNodeRenderer extends AlertNodeRenderer {

    private final MarkdownWriter writer;
    private final MarkdownNodeRendererContext context;

    public AlertMarkdownNodeRenderer(MarkdownNodeRendererContext context) {
        this.writer = context.getWriter();
        this.context = context;
    }

    @Override
    protected void renderAlert(Alert alert) {
        // First line: > [!TYPE]
        writer.writePrefix("> ");
        writer.pushPrefix("> ");
        writer.raw("[!" + alert.getType() + "]");

        // Custom title if present, also on the first line.
        var first = alert.getFirstChild();
        if (first instanceof AlertTitle) {
            writer.raw(" ");
            renderChildren(first);
        }

        writer.line();
        renderChildren(alert);
        writer.popPrefix();
        writer.block();
    }

    private void renderChildren(Node parent) {
        var node = parent.getFirstChild();
        while (node != null) {
            var next = node.getNext();

            // AlertTitle is rendered separately from other nodes.
            if (!(node instanceof AlertTitle)) {
                context.render(node);
            }
            node = next;
        }
    }
}
