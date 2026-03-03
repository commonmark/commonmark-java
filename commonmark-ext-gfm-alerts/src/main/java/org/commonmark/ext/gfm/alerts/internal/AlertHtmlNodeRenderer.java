package org.commonmark.ext.gfm.alerts.internal;

import org.commonmark.ext.gfm.alerts.Alert;
import org.commonmark.node.Node;
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.renderer.html.HtmlWriter;

import java.util.LinkedHashMap;
import java.util.Map;

public class AlertHtmlNodeRenderer extends AlertNodeRenderer {

    private final HtmlWriter htmlWriter;
    private final HtmlNodeRendererContext context;
    private final Map<String, String> customTypeTitles;

    public AlertHtmlNodeRenderer(HtmlNodeRendererContext context, Map<String, String> customTypeTitles) {
        this.htmlWriter = context.getWriter();
        this.context = context;
        this.customTypeTitles = customTypeTitles;
    }

    @Override
    protected void renderAlert(Alert alert) {
        String type = alert.getType();
        String cssClass = type.toLowerCase();

        htmlWriter.line();
        Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put("class", "markdown-alert markdown-alert-" + cssClass);
        attributes.put("data-alert-type", cssClass);

        htmlWriter.tag("div", context.extendAttributes(alert, "div", attributes));
        htmlWriter.line();

        // Render alert title
        htmlWriter.tag("p", Map.of("class", "markdown-alert-title"));
        htmlWriter.text(getAlertTitle(type));
        htmlWriter.tag("/p");
        htmlWriter.line();

        // Render children (the alert content)
        renderChildren(alert);

        htmlWriter.tag("/div");
        htmlWriter.line();
    }

    private String getAlertTitle(String type) {
        if (customTypeTitles.containsKey(type)) {
            return customTypeTitles.get(type);
        }
        switch (type) {
            case "NOTE":
                return "Note";
            case "TIP":
                return "Tip";
            case "IMPORTANT":
                return "Important";
            case "WARNING":
                return "Warning";
            case "CAUTION":
                return "Caution";
            default:
                throw new IllegalStateException("Unknown alert type: " + type);
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
