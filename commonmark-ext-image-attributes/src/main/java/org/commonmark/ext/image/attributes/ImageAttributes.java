package org.commonmark.ext.image.attributes;

import org.commonmark.node.CustomNode;
import org.commonmark.node.Delimited;

/**
 * A node containing text and other inline nodes as children.
 */
public class ImageAttributes extends CustomNode implements Delimited {

    private final String attributes;

    public ImageAttributes(String attributes) {
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

    public String getAttributes() {
        return attributes;
    }

    @Override
    protected String toStringAttributes() {
        return "imageAttributes=" + attributes;
    }
}
