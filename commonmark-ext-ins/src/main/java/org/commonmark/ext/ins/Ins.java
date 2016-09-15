package org.commonmark.ext.ins;

import org.commonmark.node.CustomNode;
import org.commonmark.node.Delimited;

/**
 * An ins node containing text and other inline nodes as children.
 */
public class Ins extends CustomNode implements Delimited {

    private static final String DELIMITER = "++";

    @Override
    public String getOpeningDelimiter() {
        return DELIMITER;
    }

    @Override
    public String getClosingDelimiter() {
        return DELIMITER;
    }
}
