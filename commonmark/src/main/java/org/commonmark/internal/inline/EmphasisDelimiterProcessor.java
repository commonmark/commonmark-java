package org.commonmark.internal.inline;

import org.commonmark.node.Emphasis;
import org.commonmark.node.Node;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Text;
import org.commonmark.parser.DelimiterProcessor;

public abstract class EmphasisDelimiterProcessor implements DelimiterProcessor {

    private final char delimiterChar;

    protected EmphasisDelimiterProcessor(char delimiterChar) {
        this.delimiterChar = delimiterChar;
    }

    @Override
    public char getOpeningDelimiterChar() {
        return delimiterChar;
    }

    @Override
    public char getClosingDelimiterChar() {
        return delimiterChar;
    }

    @Override
    public int getMinDelimiterCount() {
        return 1;
    }

    @Override
    public int getDelimiterUse(int openerCount, int closerCount) {
        // calculate actual number of delimiters used from this closer
        if (closerCount < 3 || openerCount < 3) {
            return closerCount <= openerCount ?
                    closerCount : openerCount;
        } else {
            return closerCount % 2 == 0 ? 2 : 1;
        }
    }

    @Override
    public void process(Text opener, Text closer, int delimiterUse) {
        String singleDelimiter = String.valueOf(getOpeningDelimiterChar());
        Node emphasis = delimiterUse == 1
                ? new Emphasis(singleDelimiter)
                : new StrongEmphasis(singleDelimiter + singleDelimiter);

        Node tmp = opener.getNext();
        while (tmp != null && tmp != closer) {
            Node next = tmp.getNext();
            emphasis.appendChild(tmp);
            tmp = next;
        }

        opener.insertAfter(emphasis);
    }
}
