package org.commonmark.parser.beta;

import java.util.Objects;

/**
 * Position within a {@link Scanner}. This is intentionally kept opaque so as not to expose the
 * internal structure of the Scanner.
 */
public class Position implements Comparable<Position> {

    final int lineIndex;
    final int index;

    Position(int lineIndex, int index) {
        this.lineIndex = lineIndex;
        this.index = index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Position)) {
            return false;
        }
        var position = (Position) o;
        return lineIndex == position.lineIndex && index == position.index;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lineIndex, index);
    }

    @Override
    public int compareTo(Position that) {
        var lineCmp = Integer.compare(lineIndex, that.lineIndex);
        if (lineCmp != 0) {
            return lineCmp;
        }
        return Integer.compare(index, that.index);
    }
}
