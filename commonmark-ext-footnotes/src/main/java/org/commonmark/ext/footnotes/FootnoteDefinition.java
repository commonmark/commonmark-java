package org.commonmark.ext.footnotes;

import org.commonmark.node.CustomBlock;

public class FootnoteDefinition extends CustomBlock {

    private String label;

    public FootnoteDefinition(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

