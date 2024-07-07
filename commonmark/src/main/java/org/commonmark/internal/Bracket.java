package org.commonmark.internal;

import org.commonmark.node.Text;
import org.commonmark.parser.beta.Position;

/**
 * Opening bracket for links (<code>[</code>) or images (<code>![</code>).
 */
public class Bracket {

    /**
     * The node of {@code !} if present, null otherwise.
     */
    public final Text bangNode;

    /**
     * The position of {@code !} if present, null otherwise.
     */
    public final Position bangPosition;

    /**
     * The node of {@code [}.
     */
    public final Text bracketNode;

    /**
     * The position of {@code [}.
     */
    public final Position bracketPosition;

    /**
     * The position of the content (after the opening bracket)
     */
    public final Position contentPosition;

    /**
     * Whether this is an image or link.
     */
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
     * Whether there is an unescaped bracket (opening or closing) after this opening bracket in the text parsed so far.
     */
    public boolean bracketAfter = false;

    static public Bracket link(Text bracketNode, Position bracketPosition, Position contentPosition, Bracket previous, Delimiter previousDelimiter) {
        return new Bracket(null, null, bracketNode, bracketPosition, contentPosition, previous, previousDelimiter, false);
    }

    static public Bracket image(Text bangNode, Position bangPosition, Text bracketNode, Position bracketPosition, Position contentPosition, Bracket previous, Delimiter previousDelimiter) {
        return new Bracket(bangNode, bangPosition, bracketNode, bracketPosition, contentPosition, previous, previousDelimiter, true);
    }

    private Bracket(Text bangNode, Position bangPosition, Text bracketNode, Position bracketPosition, Position contentPosition, Bracket previous, Delimiter previousDelimiter, boolean image) {
        this.bangNode = bangNode;
        this.bangPosition = bangPosition;
        this.bracketNode = bracketNode;
        this.bracketPosition = bracketPosition;
        this.contentPosition = contentPosition;
        this.image = image;
        this.previous = previous;
        this.previousDelimiter = previousDelimiter;
    }
}
