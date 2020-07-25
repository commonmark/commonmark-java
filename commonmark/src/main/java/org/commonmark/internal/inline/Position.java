package org.commonmark.internal.inline;

public class Position {
    final int index;

    Position(int index) {
        this.index = index;
    }

    // TODO: Move packages around so that this can stay package-private
    public int getIndex() {
        return index;
    }
}
