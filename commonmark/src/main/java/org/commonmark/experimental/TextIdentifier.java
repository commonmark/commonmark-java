package org.commonmark.experimental;

public abstract class TextIdentifier {
    public static final char INVALID_CHAR = '\0';
    public static final int INVALID_INDEX = Integer.MIN_VALUE;

    private final NodeBreakLinePattern nodeBreakLinePattern;
    protected int startIndex = INVALID_INDEX;
    protected int endIndex = INVALID_INDEX;

    public TextIdentifier(NodeBreakLinePattern nodeBreakLinePattern) {
        this.nodeBreakLinePattern = nodeBreakLinePattern;
    }

    public abstract void checkByCharacter(String text, char character, int index,
                                          NodePatternIdentifier nodePatternIdentifier);

    protected void reset() {
        startIndex = INVALID_INDEX;
        endIndex = INVALID_INDEX;
    }

    public NodeBreakLinePattern getNodeBreakLinePattern() {
        return nodeBreakLinePattern;
    }
}
