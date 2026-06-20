package org.commonmark.ext.gfm.alerts.internal;

import org.commonmark.ext.gfm.alerts.Alert;
import org.commonmark.ext.gfm.alerts.AlertTitle;
import org.commonmark.node.Node;
import org.commonmark.renderer.text.LineBreakRendering;
import org.commonmark.renderer.text.TextContentNodeRendererContext;
import org.commonmark.renderer.text.TextContentWriter;

import java.util.Map;

public class AlertTextContentNodeRenderer extends AlertNodeRenderer {

    private final TextContentNodeRendererContext context;
    private final TextContentWriter textContent;
    private final Map<String, String> allowedTypes;

    public AlertTextContentNodeRenderer(TextContentNodeRendererContext context, Map<String, String> allowedTypes) {
        this.context = context;
        this.textContent = context.getWriter();
        this.allowedTypes = allowedTypes;
    }

    @Override
    protected void renderAlert(Alert alert) {
        var type = alert.getType();
        var defaultTitle = allowedTypes.get(type);
        if (defaultTitle == null) {
            throw new IllegalStateException("Unknown alert type: " + type);
        }

        var first = alert.getFirstChild();
        if (first instanceof AlertTitle) {
            renderChildren(first);
        } else {
            textContent.write(defaultTitle);
        }

        if (alert.hasBody()) {
            if (context.lineBreakRendering() == LineBreakRendering.STRIP) {
                textContent.write(": ");
            } else {
                textContent.block();
            }
            renderChildren(alert);
        }

        textContent.block();
    }

    @Override
    protected void renderNode(Node node) {
        context.render(node);
    }
}
