package org.commonmark.ext.gfm.alerts;

import org.commonmark.node.CustomBlock;

/**
 * Alert block for highlighting important information using {@code [!TYPE]} syntax.
 */
public class Alert extends CustomBlock {

    private final String type;

    public Alert(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
