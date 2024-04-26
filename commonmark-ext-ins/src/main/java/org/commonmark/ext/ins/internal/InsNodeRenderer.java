package org.commonmark.ext.ins.internal;

import org.commonmark.ext.ins.Ins;
import org.commonmark.node.Node;
import org.commonmark.renderer.NodeRenderer;

import java.util.Set;

abstract class InsNodeRenderer implements NodeRenderer {

    @Override
    public Set<Class<? extends Node>> getNodeTypes() {
        return Set.of(Ins.class);
    }
}
