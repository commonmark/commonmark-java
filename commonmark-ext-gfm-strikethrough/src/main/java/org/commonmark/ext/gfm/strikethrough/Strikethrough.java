package org.commonmark.ext.gfm.strikethrough;

import org.commonmark.node.CustomNode;
import org.commonmark.node.Delimited;

/**
 * A strikethrough node containing text and other inline nodes nodes as children.
 */
public class Strikethrough extends CustomNode implements Delimited {

    private final char delimiterChar;
    private final int delimiterCount;

    public Strikethrough(char delimiterChar, int delimiterCount) {
        this.delimiterChar = delimiterChar;
        this.delimiterCount = delimiterCount;
    }

    @Override
    public char getDelimiterChar() {
        return delimiterChar;
    }

    @Override
    public int getDelimiterCount() {
        return delimiterCount;
    }

}
