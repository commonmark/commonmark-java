package org.commonmark.experimental;

public interface NodePatternIdentifier {
    void found(int startIndex, int endIndex, InternalBlocks[] internalBlocks);

    class InternalBlocks {
        private final int relativeStartIndex;
        private final int relativeEndIndex;

        public InternalBlocks(int relativeStartIndex, int relativeEndIndex) {
            this.relativeStartIndex = relativeStartIndex;
            this.relativeEndIndex = relativeEndIndex;
        }

        public int getRelativeStartIndex() {
            return relativeStartIndex;
        }

        public int getRelativeEndIndex() {
            return relativeEndIndex;
        }
    }
}
