package org.commonmark.ext.image.attributes.internal;

import org.commonmark.ext.image.attributes.ImageAttributes;
import org.commonmark.node.Image;
import org.commonmark.node.Node;
import org.commonmark.node.Nodes;
import org.commonmark.node.Text;
import org.commonmark.parser.delimiter.DelimiterProcessor;
import org.commonmark.parser.delimiter.DelimiterRun;

import java.util.*;

public class ImageAttributesDelimiterProcessor implements DelimiterProcessor {

    // Only allow a defined set of attributes to be used.
    private static final Set<String> SUPPORTED_ATTRIBUTES = Set.of("width", "height");

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
    public int process(DelimiterRun openingRun, DelimiterRun closingRun) {
        if (openingRun.length() != 1) {
            return 0;
        }

        // Check if the attributes can be applied - if the previous node is an Image, and if all the attributes are in
        // the set of SUPPORTED_ATTRIBUTES
        Text opener = openingRun.getOpener();
        Node nodeToStyle = opener.getPrevious();
        if (!(nodeToStyle instanceof Image)) {
            return 0;
        }

        List<Node> toUnlink = new ArrayList<>();
        StringBuilder content = new StringBuilder();

        for (Node node : Nodes.between(opener, closingRun.getCloser())) {
            // Only Text nodes can be used for attributes
            if (node instanceof Text) {
                content.append(((Text) node).getLiteral());
                toUnlink.add(node);
            } else {
                // This node type is not supported, so stop here (no need to check any further ones).
                return 0;
            }
        }

        Map<String, String> attributesMap = new LinkedHashMap<>();
        String attributes = content.toString();
        for (String s : attributes.split("\\s+")) {
            String[] attribute = s.split("=");
            if (attribute.length > 1 && SUPPORTED_ATTRIBUTES.contains(attribute[0].toLowerCase())) {
                attributesMap.put(attribute[0], attribute[1]);
            } else {
                // This attribute is not supported, so stop here (no need to check any further ones).
                return 0;
            }
        }

        // Unlink the tmp nodes
        for (Node node : toUnlink) {
            node.unlink();
        }

        if (attributesMap.size() > 0) {
            ImageAttributes imageAttributes = new ImageAttributes(attributesMap);

            // The new node is added as a child of the image node to which the attributes apply.
            nodeToStyle.appendChild(imageAttributes);
        }

        return 1;
    }
}
