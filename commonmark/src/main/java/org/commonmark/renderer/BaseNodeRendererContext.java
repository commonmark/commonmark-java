package org.commonmark.renderer;

import org.commonmark.node.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseNodeRendererContext<W extends Writer> implements NodeRendererContext<W> {

    private final Map<Class<? extends Node>, NodeRenderer> renderers = new HashMap<>(32);

    @Override
    public void render(Node node) {
        NodeRenderer nodeRenderer = renderers.get(node.getClass());
        if (nodeRenderer != null) {
            nodeRenderer.render(node);
        }
    }

    protected void addNodeRenderers(List<NodeRenderer> renderers) {
        // The first node renderer for a node type "wins".
        for (int i = renderers.size() - 1; i >= 0; i--) {
            NodeRenderer nodeRenderer = renderers.get(i);
            for (Class<? extends Node> nodeType : nodeRenderer.getNodeTypes()) {
                // Overwrite existing renderer
                this.renderers.put(nodeType, nodeRenderer);
            }
        }
    }
}
