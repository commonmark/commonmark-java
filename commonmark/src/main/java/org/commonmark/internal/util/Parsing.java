package org.commonmark.internal.util;

public class Parsing {
    public static int CODE_BLOCK_INDENT = 4;

    public static int columnsToNextTabStop(int column) {
        // Tab stop is 4
        return 4 - (column % 4);
    }
}
