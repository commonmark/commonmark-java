package org.commonmark.ext.styles.internal;

import org.commonmark.ext.styles.Styles;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.CustomNode;
import org.commonmark.node.Image;
import org.commonmark.node.Node;
import org.commonmark.renderer.html.AttributeProvider;

import java.util.*;

public class StylesAttributeProvider implements AttributeProvider {

    private StylesAttributeProvider() {
    }

    public static StylesAttributeProvider create() {
        return new StylesAttributeProvider();
    }

    @Override
    public void setAttributes(Node node, String tagName, final Map<String, String> attributes) {
        if (node instanceof Image) {
            node.accept(new AbstractVisitor() {
                @Override
                public void visit(CustomNode node) {
                    if (node instanceof Styles) {
                        Styles styles = (Styles) node;
                        for (String s : styles.getStyles().split("\\s+")) {
                            String[] attribute = s.split("=");
                            attributes.put(attribute[0], attribute[1]);
                        }
                        // Now that we have used the styles we remove the node.
                        styles.unlink();
                    }
                }
            });
        }
    }
}
