package org.commonmark.ext.front.matter.internal;

import java.util.Set;
import org.commonmark.ext.front.matter.YamlFrontMatterBlock;
import org.commonmark.node.Node;
import org.commonmark.renderer.NodeRenderer;

abstract class YamlFrontMatterNodeRenderer implements NodeRenderer {
    @Override
    public Set<Class<? extends Node>> getNodeTypes() {
        return Set.of(YamlFrontMatterBlock.class);
    }
}
