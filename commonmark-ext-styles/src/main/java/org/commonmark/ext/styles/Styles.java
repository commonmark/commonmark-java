package org.commonmark.ext.styles;

import org.commonmark.node.CustomNode;
import org.commonmark.node.Delimited;

/**
 * A styles node containing text and other inline nodes as children.
 */
public class Styles extends CustomNode implements Delimited {

    private final String styles;

    public Styles(String styles) {
        this.styles = styles;
    }

    @Override
    public String getOpeningDelimiter() {
        return "{";
    }

    @Override
    public String getClosingDelimiter() {
        return "}";
    }

    public String getStyles() {
        return styles;
    }

    @Override
    protected String toStringAttributes() {
        return "styles=" + styles;
    }
}
