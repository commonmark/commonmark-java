package org.commonmark.ext.gfm.strikethrough;

import org.commonmark.node.CustomNode;
import org.commonmark.node.Delimited;

/**
 * A strikethrough node containing text and other inline nodes as children.
 */
public class Strikethrough extends CustomNode implements Delimited {

    private String delimiter;

    public Strikethrough(String delimiter) {
        this.delimiter = delimiter;
    }

    @Override
    public String getOpeningDelimiter() {
        return delimiter;
    }

    @Override
    public String getClosingDelimiter() {
        return delimiter;
    }
}
