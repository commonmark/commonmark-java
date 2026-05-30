package org.commonmark.ext.front.matter.internal;

import java.util.List;
import org.commonmark.ext.front.matter.YamlFrontMatterNode;
import org.commonmark.node.Node;
import org.commonmark.renderer.markdown.MarkdownNodeRendererContext;
import org.commonmark.renderer.markdown.MarkdownWriter;

public class YamlFrontMatterMarkdownNodeRenderer extends YamlFrontMatterNodeRenderer {

    private final MarkdownWriter writer;

    public YamlFrontMatterMarkdownNodeRenderer(MarkdownNodeRendererContext context) {
        this.writer = context.getWriter();
    }

    @Override
    public void render(Node node) {
        renderBoundary();
        Node child = node.getFirstChild();
        while (child != null) {
            if (child instanceof YamlFrontMatterNode) {
                renderNode((YamlFrontMatterNode) child);
            }
            child = child.getNext();
        }
        renderBoundary();
        writer.line();
    }

    private void renderBoundary() {
        writer.raw("---");
        writer.line();
    }

    private void renderNode(YamlFrontMatterNode node) {
        var values = node.getValues();
        if (values.isEmpty()) {
            renderEmptyValue(node.getKey());
        } else if (values.size() == 1) {
            var value = values.get(0);
            if (value.contains("\n")) {
                renderMultiLineValue(node.getKey(), value.split("\n"));
            } else {
                renderSingleValue(node.getKey(), value);
            }
        } else {
            renderListValue(node.getKey(), values);
        }
    }

    private void renderEmptyValue(String key) {
        writer.raw(key + ":");
        writer.line();
    }

    private void renderSingleValue(String key, String value) {
        writer.raw(key + ": " + escapeValue(value));
        writer.line();
    }

    private void renderMultiLineValue(String key, String[] lines) {
        writer.raw(key + ": |");
        writer.line();
        for (var line : lines) {
            writer.raw("  " + line);
            writer.line();
        }
    }

    private void renderListValue(String key, List<String> values) {
        writer.raw(key + ":");
        writer.line();
        for (var value : values) {
            writer.raw("  - " + escapeValue(value));
            writer.line();
        }
    }

    private String escapeValue(String value) {
        if (needsQuoting(value)) {
            return "'" + value.replace("'", "''") + "'";
        }
        return value;
    }

    private boolean needsQuoting(String value) {
        /*
         * NOTE: Deliberately not escaping values which are balanced flow-style arrays/mappings.
         * This preserves the round-trip behaviour where these are parsed as a plain string - outputting them as-is will
         * result in a valid flow-style array/mapping in the output.
         */
        if (isFlowCollection(value)) {
            return false;
        }

        return value.isEmpty()
                // Key/value separator
                || value.contains(": ")
                // Comment indicator
                || value.contains(" #")
                // List indicator
                || value.startsWith("-")
                || value.contains("'")
                || value.contains("\"")
                // Unbalanced flow-style list
                || value.startsWith("[")
                // Unbalanced flow-style mapping
                || value.startsWith("{");
    }

    private boolean isFlowCollection(String value) {
        return (value.startsWith("[") && value.endsWith("]"))
                || (value.startsWith("{") && value.endsWith("}"));
    }
}
