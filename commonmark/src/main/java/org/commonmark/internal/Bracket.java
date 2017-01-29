package org.commonmark.internal;

import org.commonmark.node.Text;

/**
 * Opening bracket for links (<code>[</code>) or images (<code>![</code>).
 */
public class Bracket {

    public final Text node;
    public final int index;
    public final boolean image;

    /**
     * Previous bracket.
     */
    public final Bracket previous;

    /**
     * Previous delimiter (emphasis, etc) before this bracket.
     */
    public final Delimiter previousDelimiter;

    /**
     * Whether this bracket is allowed to form a link/image (also known as "active").
     */
    public boolean allowed = true;

    /**
     * Whether there is an unescaped bracket (opening or closing) anywhere after this opening bracket.
     */
    public boolean bracketAfter = false;

    static public Bracket link(Text node, int index, Bracket previous, Delimiter previousDelimiter) {
        return new Bracket(node, index, previous, previousDelimiter, false);
    }

    static public Bracket image(Text node, int index, Bracket previous, Delimiter previousDelimiter) {
        return new Bracket(node, index, previous, previousDelimiter, true);
    }

    private Bracket(Text node, int index, Bracket previous, Delimiter previousDelimiter, boolean image) {
        this.node = node;
        this.index = index;
        this.image = image;
        this.previous = previous;
        this.previousDelimiter = previousDelimiter;
    }
}
