package org.commonmark.internal.renderer;

import org.commonmark.node.Node;
import org.commonmark.renderer.NodeRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NodeRendererMap {

    private final List<NodeRenderer> nodeRenderers = new ArrayList<>();
    private final Map<Class<? extends Node>, NodeRenderer> renderers = new HashMap<>(32);

    public void add(NodeRenderer nodeRenderer) {
        nodeRenderers.add(nodeRenderer);
        for (var nodeType : nodeRenderer.getNodeTypes()) {
            // The first node renderer for a node type "wins".
            renderers.putIfAbsent(nodeType, nodeRenderer);
        }
    }

    public void render(Node node) {
        var nodeRenderer = renderers.get(node.getClass());
        if (nodeRenderer != null) {
            nodeRenderer.render(node);
        }
    }

    public void beforeRoot(Node node) {
        nodeRenderers.forEach(r -> r.beforeRoot(node));
    }

    public void afterRoot(Node node) {
        nodeRenderers.forEach(r -> r.afterRoot(node));
    }
}
