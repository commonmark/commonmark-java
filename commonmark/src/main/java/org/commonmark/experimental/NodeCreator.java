package org.commonmark.experimental;

import org.commonmark.node.Node;

public interface NodeCreator {
    Node build(String textFound, NodePatternIdentifier.InternalBlocks[] internalBlocks);
}
