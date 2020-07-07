package org.commonmark.experimental;

import org.commonmark.node.Node;

public interface NodeCreator {
    Node build(String found, NodePatternIdentifier.InternalBlocks[] internalBlocks);
}
