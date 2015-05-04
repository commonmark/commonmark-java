package org.commonmark.extras.tables;

import org.commonmark.node.CustomNode;

public class TableCell extends CustomNode {

    private boolean header;

    public void setHeader(boolean header) {
        this.header = header;
    }

    public boolean isHeader() {
        return header;
    }
}
