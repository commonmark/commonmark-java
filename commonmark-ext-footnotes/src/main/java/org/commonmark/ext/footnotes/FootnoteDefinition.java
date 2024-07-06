package org.commonmark.ext.footnotes;

import org.commonmark.node.CustomBlock;

/**
 * A footnote definition, e.g.:
 * <pre><code>
 * [^foo]: This is the footnote text
 * </code></pre>
 * The {@link #getLabel() label} is the text in brackets after {@code ^}, so {@code foo} in the example. The contents
 * of the footnote are child nodes of the definition, a {@link org.commonmark.node.Paragraph} in the example.
 * <p>
 * Footnote definitions are parsed even if there's no corresponding {@link FootnoteReference}.
 */
public class FootnoteDefinition extends CustomBlock {

    private String label;

    public FootnoteDefinition(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

