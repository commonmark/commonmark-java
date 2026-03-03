package org.commonmark.ext.gfm.alerts.internal;

import org.commonmark.ext.gfm.alerts.Alert;
import org.commonmark.renderer.NodeRenderer;

import java.util.Set;

public abstract class AlertNodeRenderer implements NodeRenderer {

    @Override
    public Set<Class<? extends org.commonmark.node.Node>> getNodeTypes() {
        return Set.of(Alert.class);
    }

    @Override
    public void render(org.commonmark.node.Node node) {
        Alert alert = (Alert) node;
        renderAlert(alert);
    }

    protected abstract void renderAlert(Alert alert);
}
