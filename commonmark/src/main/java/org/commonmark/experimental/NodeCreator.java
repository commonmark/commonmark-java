package org.commonmark.experimental;

public interface NodeCreator {
    String build(String found, NodePatternIdentifier.InternalBlocks[] internalBlocks);
}
