package org.commonmark.internal.inline;

public class Position {
    final int lineIndex;
    final int index;

    Position(int lineIndex, int index) {
        this.lineIndex = lineIndex;
        this.index = index;
    }

    // TODO: Move packages around so that this can stay package-private
    public int getLineIndex() {
        return lineIndex;
    }

    // TODO: Move packages around so that this can stay package-private
    public int getIndex() {
        return index;
    }
}
