package org.commonmark.ext.footnotes;

import org.commonmark.node.CustomNode;

public class FootnoteReference extends CustomNode {
    private String label;

    public FootnoteReference(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
