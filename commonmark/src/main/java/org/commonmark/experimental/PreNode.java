package org.commonmark.experimental;

public class PreNode {
    private final NodeCreator nodeCreator;
    private final int startIndex;
    private final int endIndex;
    private final int priority;
    private final NodePatternIdentifier.InternalBlocks[] internalBlocks;

    public PreNode(NodeCreator nodeCreator, int priority, int startIndex, int endIndex,
                   NodePatternIdentifier.InternalBlocks[] internalBlocks) {
        this.nodeCreator = nodeCreator;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.priority = priority;
        this.internalBlocks = internalBlocks;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public int getPriority() {
        return priority;
    }

    public NodeCreator getNodeCreator() {
        return nodeCreator;
    }

    public NodePatternIdentifier.InternalBlocks[] getInternalBlocks() {
        return internalBlocks;
    }
}
