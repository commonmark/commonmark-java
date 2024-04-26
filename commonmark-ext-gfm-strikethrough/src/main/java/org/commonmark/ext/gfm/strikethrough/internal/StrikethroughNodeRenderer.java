package org.commonmark.ext.gfm.strikethrough.internal;

import org.commonmark.ext.gfm.strikethrough.Strikethrough;
import org.commonmark.node.Node;
import org.commonmark.renderer.NodeRenderer;

import java.util.Set;

abstract class StrikethroughNodeRenderer implements NodeRenderer {

    @Override
    public Set<Class<? extends Node>> getNodeTypes() {
        return Set.of(Strikethrough.class);
    }
}
