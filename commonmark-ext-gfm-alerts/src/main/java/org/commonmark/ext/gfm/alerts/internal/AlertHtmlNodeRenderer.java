package org.commonmark.ext.gfm.alerts.internal;

import org.commonmark.ext.gfm.alerts.Alert;
import org.commonmark.ext.gfm.alerts.AlertTitle;
import org.commonmark.node.Node;
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.renderer.html.HtmlWriter;

import java.util.LinkedHashMap;
import java.util.Map;

public class AlertHtmlNodeRenderer extends AlertNodeRenderer {

    private final HtmlWriter htmlWriter;
    private final HtmlNodeRendererContext context;
    private final Map<String, String> allowedTypes;

    public AlertHtmlNodeRenderer(HtmlNodeRendererContext context, Map<String, String> allowedTypes) {
        this.htmlWriter = context.getWriter();
        this.context = context;
        this.allowedTypes = allowedTypes;
    }

    @Override
    protected void renderAlert(Alert alert) {
        var type = alert.getType();
        if (!allowedTypes.containsKey(type)) {
            throw new IllegalStateException("Unknown alert type: " + type);
        }
        var cssClass = type.toLowerCase();

        htmlWriter.line();
        var attributes = new LinkedHashMap<String, String>();
        attributes.put("class", "markdown-alert markdown-alert-" + cssClass);
        attributes.put("data-alert-type", cssClass);

        htmlWriter.tag("div", context.extendAttributes(alert, "div", attributes));
        htmlWriter.line();

        // Render alert title
        htmlWriter.tag("p", context.extendAttributes(alert, "p", Map.of("class", "markdown-alert-title")));
        var first = alert.getFirstChild();
        if (first instanceof AlertTitle) {
            renderChildren(first);
        } else {
            htmlWriter.text(allowedTypes.get(type));
        }
        htmlWriter.tag("/p");
        htmlWriter.line();

        // Render children (the alert content)
        renderChildren(alert);

        htmlWriter.tag("/div");
        htmlWriter.line();
    }

    @Override
    protected void renderNode(Node node) {
        context.render(node);
    }
}
