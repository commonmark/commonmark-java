package org.commonmark.ext.gfm.alerts.internal;

import org.commonmark.ext.gfm.alerts.Alert;
import org.commonmark.ext.gfm.alerts.AlertTitle;
import org.commonmark.node.Node;
import org.commonmark.renderer.NodeRenderer;

import java.util.Set;

public abstract class AlertNodeRenderer implements NodeRenderer {

    @Override
    public Set<Class<? extends Node>> getNodeTypes() {
        return Set.of(Alert.class);
    }

    @Override
    public void render(Node node) {
        var alert = (Alert) node;
        renderAlert(alert);
    }

    protected abstract void renderAlert(Alert alert);

    /**
     * Renders the children of a parent node, excluding {@link AlertTitle} nodes.
     * {@link AlertTitle} is rendered separately from other content.
     *
     * @param parent the parent node whose children should be rendered
     */
    protected final void renderChildren(Node parent) {
        var node = parent.getFirstChild();
        while (node != null) {
            var next = node.getNext();

            // AlertTitle is rendered separately from other nodes.
            if (!(node instanceof AlertTitle)) {
                renderNode(node);
            }
            node = next;
        }
    }

    /**
     * Renders a single node. Subclasses must implement this to delegate
     * to their context's render method.
     *
     * @param node the node to render
     */
    protected abstract void renderNode(Node node);
}
