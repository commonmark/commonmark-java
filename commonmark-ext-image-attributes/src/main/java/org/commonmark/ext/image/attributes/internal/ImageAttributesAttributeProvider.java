package org.commonmark.ext.image.attributes.internal;

import org.commonmark.ext.image.attributes.ImageAttributes;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.CustomNode;
import org.commonmark.node.Image;
import org.commonmark.node.Node;
import org.commonmark.renderer.html.AttributeProvider;

import java.util.*;

public class ImageAttributesAttributeProvider implements AttributeProvider {

    private ImageAttributesAttributeProvider() {
    }

    public static ImageAttributesAttributeProvider create() {
        return new ImageAttributesAttributeProvider();
    }

    @Override
    public void setAttributes(Node node, String tagName, final Map<String, String> attributes) {
        if (node instanceof Image) {
            node.accept(new AbstractVisitor() {
                @Override
                public void visit(CustomNode node) {
                    if (node instanceof ImageAttributes) {
                        ImageAttributes imageAttributes = (ImageAttributes) node;
                        Map<String, String> attributesMap = imageAttributes.getAttributes();
                        for (String key : attributesMap.keySet()) {
                            attributes.put(key, attributesMap.get(key));
                        }
                        // Now that we have used the image attributes we remove the node.
                        imageAttributes.unlink();
                    }
                }
            });
        }
    }
}
