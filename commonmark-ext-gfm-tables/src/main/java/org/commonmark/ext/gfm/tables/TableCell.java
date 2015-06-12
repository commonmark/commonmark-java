package org.commonmark.ext.gfm.tables;

import org.commonmark.node.CustomNode;

public class TableCell extends CustomNode {

    private boolean header;
    private Alignment alignment;

    public boolean isHeader() {
        return header;
    }

    public void setHeader(boolean header) {
        this.header = header;
    }

    public Alignment getAlignment() {
        return alignment;
    }

    public void setAlignment(Alignment alignment) {
        this.alignment = alignment;
    }

    public enum Alignment {
        LEFT, CENTER, RIGHT
    }
}
