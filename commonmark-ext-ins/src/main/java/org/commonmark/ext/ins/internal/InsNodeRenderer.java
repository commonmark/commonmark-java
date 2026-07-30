package org.commonmark.ext.ins.internal;

import java.util.Set;
import org.commonmark.ext.ins.Ins;
import org.commonmark.node.Node;
import org.commonmark.renderer.NodeRenderer;

abstract class InsNodeRenderer implements NodeRenderer {

    @Override
    public Set<Class<? extends Node>> getNodeTypes() {
        return Set.of(Ins.class);
    }
}
