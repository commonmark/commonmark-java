package org.commonmark.experimental;

import org.commonmark.experimental.NodePatternIdentifier.InternalBlocks;
import org.commonmark.node.Node;

public interface NodeCreator {
    Node build(String textFound, InternalBlocks[] internalBlocks);
}
