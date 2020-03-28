package org.commonmark.ext.styles.internal;

import org.commonmark.ext.styles.Styles;
import org.commonmark.node.Image;
import org.commonmark.node.Node;
import org.commonmark.node.Text;
import org.commonmark.parser.delimiter.DelimiterProcessor;
import org.commonmark.parser.delimiter.DelimiterRun;

import java.util.*;

public class StylesDelimiterProcessor implements DelimiterProcessor {

    // Only allow a defined set of styles to be used.
    private static final Set<String> SUPPORTED_STYLES = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("width", "height")));

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
        // Check if the attributes can be applied - if the previous node is an Image, and if all the attributes are in
        // the set of SUPPORTED_STYLES
        if (opener.getPrevious() instanceof Image) {
            boolean canApply = true;
            List<Node> toUnlink = new ArrayList<>();

            StringBuilder styleString = new StringBuilder();
            Node tmp = opener.getNext();
            while (tmp != null && tmp != closer) {
                Node next = tmp.getNext();
                // Only Text nodes can be used for attributes
                if (tmp instanceof Text) {
                    String styles = ((Text) tmp).getLiteral();
                    for (String s : styles.split("\\s+")) {
                        String[] attribute = s.split("=");
                        // Check if the attribute is in SUPPORTED_STYLES.
                        if (SUPPORTED_STYLES.contains(attribute[0].toLowerCase())) {
                            styleString.append(((Text) tmp).getLiteral());
                            // The tmp node can be unlinked, as we have retrieved its value.
                            toUnlink.add(tmp);
                        } else {
                            // This attribute is not supported, so break here (no need to check any further ones).
                            canApply = false;
                            break;
                        }
                    }
                } else {
                    // This node type is not supported, so break here (no need to check any further ones).
                    canApply = false;
                    break;
                }
                tmp = next;
            }

            // Only if all of the above checks pass can the attributes be applied.
            if (canApply) {
                // Unlink the tmp nodes
                for (Node node : toUnlink) {
                    node.unlink();
                }

                if (styleString.length() > 0) {
                    Styles styles = new Styles(styleString.toString());

                    // The styles node is added as a child of the node to which the styles apply.
                    Node nodeToStyle = opener.getPrevious();
                    nodeToStyle.appendChild(styles);
                }
                return;
            }
        }

        // If we got here then the attributes cannot be applied, so fallback to leaving the text unchanged.
        // Need to add back the opening and closing characters (which are removed elsewhere).
        if (opener.getPrevious() == null) {
            opener.getParent().prependChild(new Text("" + getOpeningCharacter()));
        } else {
            opener.getPrevious().insertAfter(new Text("" + getOpeningCharacter()));
        }
        closer.getParent().appendChild(new Text("" + getClosingCharacter()));
    }

    /**
     * Check that the attributes can be applied to the previous node.
     * @param opener the text node that contained the opening delimiter
     * @param closer the text node that contained the closing delimiter
     * @return true if the previous node is an Image and the attributes are in the set of {@link #SUPPORTED_STYLES}
     */
    private boolean canApply(Text opener, Text closer) {
        if (!(opener.getPrevious() instanceof Image)) {
            return false;
        }

        Node tmp = opener.getNext();
        while (tmp != null && tmp != closer) {
            Node next = tmp.getNext();
            if (tmp instanceof Text) {
                String styles = ((Text) tmp).getLiteral();
                for (String s : styles.split("\\s+")) {
                    String[] attribute = s.split("=");
                    if (!SUPPORTED_STYLES.contains(attribute[0].toLowerCase())) {
                        return false;
                    }
                }
            } else {
                return false;
            }
            tmp = next;
        }
        return true;
    }
}
