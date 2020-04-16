package org.commonmark.ext.image.attributes;

import org.commonmark.node.CustomNode;
import org.commonmark.node.Delimited;

import java.util.Map;

/**
 * A node containing text and other inline nodes as children.
 */
public class ImageAttributes extends CustomNode implements Delimited {

    private final Map<String, String> attributes;

    public ImageAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getOpeningDelimiter() {
        return "{";
    }

    @Override
    public String getClosingDelimiter() {
        return "}";
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    @Override
    protected String toStringAttributes() {
        return "imageAttributes=" + attributes;
    }
}
