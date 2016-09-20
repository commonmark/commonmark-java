package org.commonmark.renderer.html;

import org.commonmark.node.Node;

import java.util.Map;

/**
 * Extension point for adding/changing attributes on the primary HTML tag for a node.
 */
public interface AttributeProvider {

    /**
     * Set the attributes for the node by modifying the provided map.
     * <p>
     * This allows to change or even remove default attributes. With great power comes great responsibility.
     * <p>
     * The attribute key and values will be escaped (preserving character entities), so don't escape them here,
     * otherwise they will be double-escaped.
     *
     * @param node the node to set attributes for
     * @param attributes the attributes, with any default attributes already set in the map
     */
    void setAttributes(Node node, Map<String, String> attributes);

}
