package org.commonmark.ext.gfm.tables.internal;

import org.commonmark.ext.gfm.tables.*;
import org.commonmark.node.Block;
import org.commonmark.node.Node;
import org.commonmark.node.SourceSpan;
import org.commonmark.parser.InlineParser;
import org.commonmark.parser.block.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TableBlockParser extends AbstractBlockParser {

    private final TableBlock block = new TableBlock();
    private final List<CharSequence> rowLines = new ArrayList<>();
    private final List<TableCell.Alignment> columns;

    private TableBlockParser(List<TableCell.Alignment> columns, CharSequence headerLine) {
        this.columns = columns;
        this.rowLines.add(headerLine);
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
        rowLines.add(line);
    }

    @Override
    public void parseInlines(InlineParser inlineParser) {
        List<SourceSpan> sourceSpans = block.getSourceSpans();

        SourceSpan headerSourceSpan = !sourceSpans.isEmpty() ? sourceSpans.get(0) : null;
        Node head = new TableHead();
        if (headerSourceSpan != null) {
            head.addSourceSpan(headerSourceSpan);
        }
        block.appendChild(head);

        TableRow headerRow = new TableRow();
        headerRow.setSourceSpans(head.getSourceSpans());
        head.appendChild(headerRow);

        List<CellSource> headerCells = split(rowLines.get(0), headerSourceSpan);
        int headerColumns = headerCells.size();
        for (int i = 0; i < headerColumns; i++) {
            CellSource cell = headerCells.get(i);
            TableCell tableCell = parseCell(cell, i, inlineParser);
            tableCell.setHeader(true);
            headerRow.appendChild(tableCell);
        }

        TableBody body = null;
        // Body starts at index 2. 0 is header, 1 is separator.
        for (int rowIndex = 2; rowIndex < rowLines.size(); rowIndex++) {
            CharSequence rowLine = rowLines.get(rowIndex);
            SourceSpan sourceSpan = rowIndex < sourceSpans.size() ? sourceSpans.get(rowIndex) : null;
            List<CellSource> cells = split(rowLine, sourceSpan);
            TableRow row = new TableRow();
            row.addSourceSpan(sourceSpan);

            // Body can not have more columns than head
            for (int i = 0; i < headerColumns; i++) {
                CellSource cell = i < cells.size() ? cells.get(i) : new CellSource("", null);
                TableCell tableCell = parseCell(cell, i, inlineParser);
                row.appendChild(tableCell);
            }

            if (body == null) {
                // It's valid to have a table without body. In that case, don't add an empty TableBody node.
                body = new TableBody();
                block.appendChild(body);
            }
            body.appendChild(row);
            body.addSourceSpan(sourceSpan);
        }
    }

    private TableCell parseCell(CellSource cell, int column, InlineParser inlineParser) {
        TableCell tableCell = new TableCell();

        if (column < columns.size()) {
            tableCell.setAlignment(columns.get(column));
        }

        if (cell.sourceSpan != null) {
            tableCell.setSourceSpans(Collections.singletonList(cell.sourceSpan));
        }

        inlineParser.parse(Collections.<CharSequence>singletonList(cell.content.trim()), tableCell);

        return tableCell;
    }

    private static List<CellSource> split(CharSequence row, SourceSpan rowSourceSpan) {
        int cellStart = row.charAt(0) == '|' ? 1 : 0;
        List<CellSource> cells = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (int i = cellStart; i < row.length(); i++) {
            char c = row.charAt(i);
            switch (c) {
                case '\\':
                    if (i + 1 < row.length() && row.charAt(i + 1) == '|') {
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
                    String content = sb.toString();
                    cells.add(CellSource.of(content, rowSourceSpan, cellStart));
                    sb.setLength(0);
                    // + 1 to skip the pipe itself for the next cell's span
                    cellStart = i + 1;
                    break;
                default:
                    sb.append(c);
            }
        }
        if (sb.length() > 0) {
            String content = sb.toString();
            cells.add(CellSource.of(content, rowSourceSpan, cellStart));
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
            List<CharSequence> paragraphLines = matchedBlockParser.getParagraphLines();
            if (paragraphLines.size() == 1 && paragraphLines.get(0).toString().contains("|")) {
                CharSequence separatorLine = line.subSequence(state.getIndex(), line.length());
                List<TableCell.Alignment> columns = parseSeparator(separatorLine);
                if (columns != null && !columns.isEmpty()) {
                    CharSequence paragraph = paragraphLines.get(0);
                    List<CellSource> headerCells = split(paragraph, null);
                    if (columns.size() >= headerCells.size()) {
                        return BlockStart.of(new TableBlockParser(columns, paragraph))
                                .atIndex(state.getIndex())
                                .replaceActiveBlockParser();
                    }
                }
            }
            return BlockStart.none();
        }
    }

    private static class CellSource {

        private final String content;
        private final SourceSpan sourceSpan;

        public static CellSource of(String content, SourceSpan rowSourceSpan, int offset) {
            if (!content.isEmpty()) {
                SourceSpan sourceSpan = null;
                if (rowSourceSpan != null) {
                    sourceSpan = SourceSpan.of(rowSourceSpan.getLineIndex(), rowSourceSpan.getColumnIndex() + offset, content.length());
                }
                return new CellSource(content, sourceSpan);
            } else {
                return new CellSource(content, null);
            }
        }

        private CellSource(String content, SourceSpan sourceSpan) {
            this.content = content;
            this.sourceSpan = sourceSpan;
        }
    }
}
