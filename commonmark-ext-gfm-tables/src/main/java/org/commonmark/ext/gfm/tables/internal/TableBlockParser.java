package org.commonmark.ext.gfm.tables.internal;

import org.commonmark.ext.gfm.tables.*;
import org.commonmark.node.Block;
import org.commonmark.node.Node;
import org.commonmark.parser.InlineParser;
import org.commonmark.parser.block.*;

import java.util.ArrayList;
import java.util.List;

public class TableBlockParser extends AbstractBlockParser {

    private final TableBlock block = new TableBlock();
    private final List<CharSequence> bodyLines = new ArrayList<>();
    private final List<TableCell.Alignment> columns;
    private final List<String> headerCells;

    private boolean nextIsSeparatorLine = true;

    private TableBlockParser(List<TableCell.Alignment> columns, List<String> headerCells) {
        this.columns = columns;
        this.headerCells = headerCells;
    }

    @Override
    public boolean canHaveLazyContinuationLines() {
        return true;
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public BlockContinue tryContinue(ParserState state) {
        if (state.getLine().toString().contains("|")) {
            return BlockContinue.atIndex(state.getIndex());
        } else {
            return BlockContinue.none();
        }
    }

    @Override
    public void addLine(CharSequence line) {
        if (nextIsSeparatorLine) {
            nextIsSeparatorLine = false;
        } else {
            bodyLines.add(line);
        }
    }

    @Override
    public void parseInlines(InlineParser inlineParser) {
        int headerColumns = headerCells.size();

        Node head = new TableHead();
        block.appendChild(head);

        TableRow headerRow = new TableRow();
        head.appendChild(headerRow);
        for (int i = 0; i < headerColumns; i++) {
            String cell = headerCells.get(i);
            TableCell tableCell = parseCell(cell, i, inlineParser);
            tableCell.setHeader(true);
            headerRow.appendChild(tableCell);
        }

        Node body = null;
        for (CharSequence rowLine : bodyLines) {
            List<String> cells = split(rowLine);
            TableRow row = new TableRow();

            // Body can not have more columns than head
            for (int i = 0; i < headerColumns; i++) {
                String cell = i < cells.size() ? cells.get(i) : "";
                TableCell tableCell = parseCell(cell, i, inlineParser);
                row.appendChild(tableCell);
            }

            if (body == null) {
                // It's valid to have a table without body. In that case, don't add an empty TableBody node.
                body = new TableBody();
                block.appendChild(body);
            }
            body.appendChild(row);
        }
    }

    private TableCell parseCell(String cell, int column, InlineParser inlineParser) {
        TableCell tableCell = new TableCell();

        if (column < columns.size()) {
            tableCell.setAlignment(columns.get(column));
        }

        inlineParser.parse(cell.trim(), tableCell);

        return tableCell;
    }

    private static List<String> split(CharSequence input) {
        String line = input.toString().trim();
        if (line.startsWith("|")) {
            line = line.substring(1);
        }
        List<String> cells = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            switch (c) {
                case '\\':
                    if (i + 1 < line.length() && line.charAt(i + 1) == '|') {
                        // Pipe is special for table parsing. An escaped pipe doesn't result in a new cell, but is
                        // passed down to inline parsing as an unescaped pipe. Note that that applies even for the `\|`
                        // in an input like `\\|` - in other words, table parsing doesn't support escaping backslashes.
                        sb.append('|');
                        i++;
                    } else {
                        // Preserve backslash before other characters or at end of line.
                        sb.append('\\');
                    }
                    break;
                case '|':
                    cells.add(sb.toString());
                    sb.setLength(0);
                    break;
                default:
                    sb.append(c);
            }
        }
        if (sb.length() > 0) {
            cells.add(sb.toString());
        }
        return cells;
    }

    // Examples of valid separators:
    //
    // |-
    // -|
    // |-|
    // -|-
    // |-|-|
    // --- | ---
    private static List<TableCell.Alignment> parseSeparator(CharSequence s) {
        List<TableCell.Alignment> columns = new ArrayList<>();
        int pipes = 0;
        boolean valid = false;
        int i = 0;
        while (i < s.length()) {
            char c = s.charAt(i);
            switch (c) {
                case '|':
                    i++;
                    pipes++;
                    if (pipes > 1) {
                        // More than one adjacent pipe not allowed
                        return null;
                    }
                    // Need at lest one pipe, even for a one column table
                    valid = true;
                    break;
                case '-':
                case ':':
                    if (pipes == 0 && !columns.isEmpty()) {
                        // Need a pipe after the first column (first column doesn't need to start with one)
                        return null;
                    }
                    boolean left = false;
                    boolean right = false;
                    if (c == ':') {
                        left = true;
                        i++;
                    }
                    boolean haveDash = false;
                    while (i < s.length() && s.charAt(i) == '-') {
                        i++;
                        haveDash = true;
                    }
                    if (!haveDash) {
                        // Need at least one dash
                        return null;
                    }
                    if (i < s.length() && s.charAt(i) == ':') {
                        right = true;
                        i++;
                    }
                    columns.add(getAlignment(left, right));
                    // Next, need another pipe
                    pipes = 0;
                    break;
                case ' ':
                case '\t':
                    // White space is allowed between pipes and columns
                    i++;
                    break;
                default:
                    // Any other character is invalid
                    return null;
            }
        }
        if (!valid) {
            return null;
        }
        return columns;
    }

    private static TableCell.Alignment getAlignment(boolean left, boolean right) {
        if (left && right) {
            return TableCell.Alignment.CENTER;
        } else if (left) {
            return TableCell.Alignment.LEFT;
        } else if (right) {
            return TableCell.Alignment.RIGHT;
        } else {
            return null;
        }
    }

    public static class Factory extends AbstractBlockParserFactory {

        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            CharSequence line = state.getLine();
            CharSequence paragraph = matchedBlockParser.getParagraphContent();
            if (paragraph != null && paragraph.toString().contains("|") && !paragraph.toString().contains("\n")) {
                CharSequence separatorLine = line.subSequence(state.getIndex(), line.length());
                List<TableCell.Alignment> columns = parseSeparator(separatorLine);
                if (columns != null && !columns.isEmpty()) {
                    List<String> headerCells = split(paragraph);
                    if (columns.size() >= headerCells.size()) {
                        return BlockStart.of(new TableBlockParser(columns, headerCells))
                                .atIndex(state.getIndex())
                                .replaceActiveBlockParser();
                    }
                }
            }
            return BlockStart.none();
        }
    }

}
