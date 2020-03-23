package org.commonmark.ext.styles.internal;

import org.commonmark.ext.styles.Styles;
import org.commonmark.node.Node;
import org.commonmark.node.Text;
import org.commonmark.parser.delimiter.DelimiterProcessor;
import org.commonmark.parser.delimiter.DelimiterRun;

public class StylesDelimiterProcessor implements DelimiterProcessor {

    @Override
    public char getOpeningCharacter() {
        return '{';
    }

    @Override
    public char getClosingCharacter() {
        return '}';
    }

    @Override
    public int getMinLength() {
        return 1;
    }

    @Override
    public int getDelimiterUse(DelimiterRun opener, DelimiterRun closer) {
        return 1;
    }

    @Override
    public void process(Text opener, Text closer, int delimiterCount) {
        StringBuilder styleString = new StringBuilder();
        Node tmp = opener.getNext();
        while (tmp != null && tmp != closer) {
            Node next = tmp.getNext();
            if (tmp instanceof Text) {
                styleString.append(((Text) tmp).getLiteral());
                // Unlink the tmp node, now that we have retrieved its value.
                tmp.unlink();
            }
            tmp = next;
        }

        Styles styles = new Styles(styleString.toString());

        // The styles node is added as a child of the node to which the styles apply.
        Node nodeToStyle = opener.getPrevious();
        nodeToStyle.appendChild(styles);
    }
}
