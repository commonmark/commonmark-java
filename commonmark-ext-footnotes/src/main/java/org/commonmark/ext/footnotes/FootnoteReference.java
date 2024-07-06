package org.commonmark.ext.footnotes;

import org.commonmark.node.CustomNode;

/**
 * A footnote reference, e.g. <code>[^foo]</code> in <code>Some text with a footnote[^foo]</code>
 * <p>
 * The {@link #getLabel() label} is the text within brackets after {@code ^}, so {@code foo} in the example. It needs to
 * match the label of a corresponding {@link FootnoteDefinition} for the footnote to be parsed.
 */
public class FootnoteReference extends CustomNode {
    private String label;

    public FootnoteReference(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
