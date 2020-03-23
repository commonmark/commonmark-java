package org.commonmark.ext.styles.internal;

import org.commonmark.ext.styles.Styles;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.CustomNode;
import org.commonmark.node.Image;
import org.commonmark.node.Node;
import org.commonmark.renderer.html.AttributeProvider;

import java.util.*;

public class StylesAttributeProvider implements AttributeProvider {

    // Only allow a defined set of styles to be used.
    private static final Set<String> SUPPORTED_STYLES = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("width", "height")));

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
                        for (String s: styles.getStyles().split(" ")) {
                            String[] attribute = s.split("=");
                            if (SUPPORTED_STYLES.contains(attribute[0].toLowerCase())) {
                                attributes.put(attribute[0], attribute[1]);
                            }
                        }
                        // Now that we have used the styles we remove the node.
                        styles.unlink();
                    }
                }
            });
        }
    }
}
