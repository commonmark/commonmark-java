package org.commonmark.node;

import java.util.Objects;

public class SourceSpan {

    private final int lineIndex;
    private final int columnIndex;
    private final int length;

    public static SourceSpan of(int lineIndex, int columnIndex, int length) {
        return new SourceSpan(lineIndex, columnIndex, length);
    }

    private SourceSpan(int lineIndex, int columnIndex, int length) {
        this.lineIndex = lineIndex;
        this.columnIndex = columnIndex;
        this.length = length;
    }

    /**
     * @return 0-based index of line in source
     */
    public int getLineIndex() {
        return lineIndex;
    }

    /**
     * @return 0-based index of column (character on line) in source
     */
    public int getColumnIndex() {
        return columnIndex;
    }

    /**
     * @return length of the span
     */
    public int getLength() {
        return length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SourceSpan that = (SourceSpan) o;
        return lineIndex == that.lineIndex &&
                columnIndex == that.columnIndex &&
                length == that.length;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lineIndex, columnIndex, length);
    }

    @Override
    public String toString() {
        return "SourceSpan{" +
                "lineIndex=" + lineIndex +
                ", columnIndex=" + columnIndex +
                ", length=" + length +
                "}";
    }
}
