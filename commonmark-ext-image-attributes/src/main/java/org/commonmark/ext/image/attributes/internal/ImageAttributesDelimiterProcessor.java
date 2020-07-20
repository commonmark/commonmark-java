package org.commonmark.ext.image.attributes.internal;

import org.commonmark.ext.image.attributes.ImageAttributes;
import org.commonmark.node.Image;
import org.commonmark.node.Node;
import org.commonmark.node.Text;
import org.commonmark.parser.delimiter.DelimiterProcessor;
import org.commonmark.parser.delimiter.DelimiterRun;

import java.util.*;

public class ImageAttributesDelimiterProcessor implements DelimiterProcessor {

    // Only allow a defined set of attributes to be used.
    private static final Set<String> SUPPORTED_ATTRIBUTES = Collections.unmodifiableSet(
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
        // the set of SUPPORTED_ATTRIBUTES
        if (opener.getPrevious() instanceof Image) {
            boolean canApply = true;
            List<Node> toUnlink = new ArrayList<>();

            Map<String, String> attributesMap = new LinkedHashMap<>();
            Node tmp = opener.getNext();
            while (tmp != null && tmp != closer) {
                Node next = tmp.getNext();
                // Only Text nodes can be used for attributes
                if (tmp instanceof Text) {
                    String attributes = ((Text) tmp).getLiteral();
                    for (String s : attributes.split("\\s+")) {
                        String[] attribute = s.split("=");
                        if (attribute.length > 1 && SUPPORTED_ATTRIBUTES.contains(attribute[0].toLowerCase())) {
                            attributesMap.put(attribute[0], attribute[1]);
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

                if (attributesMap.size() > 0) {
                    ImageAttributes imageAttributes = new ImageAttributes(attributesMap);

                    // The new node is added as a child of the image node to which the attributes apply.
                    Node nodeToStyle = opener.getPrevious();
                    nodeToStyle.appendChild(imageAttributes);
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
        closer.insertAfter(new Text("" + getClosingCharacter()));
    }
}
