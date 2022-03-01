package org.commonmark.ext.gfm.tables.internal;

import org.commonmark.ext.gfm.tables.*;
import org.commonmark.internal.util.Parsing;
import org.commonmark.node.Block;
import org.commonmark.node.Node;
import org.commonmark.node.SourceSpan;
import org.commonmark.parser.InlineParser;
import org.commonmark.parser.SourceLine;
import org.commonmark.parser.SourceLines;
import org.commonmark.parser.block.*;

import java.util.ArrayList;
import java.util.List;

public class TableBlockParser extends AbstractBlockParser {

    private final TableBlock block = new TableBlock();
    private final List<SourceLine> rowLines = new ArrayList<>();
    private final List<TableCell.Alignment> columns;

    private TableBlockParser(List<TableCell.Alignment> columns, SourceLine headerLine) {
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
        if (Parsing.find('|', state.getLine().getContent(), 0) != -1) {
            return BlockContinue.atIndex(state.getIndex());
        } else {
            return BlockContinue.none();
        }
    }

    @Override
    public void addLine(SourceLine line) {
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

        List<SourceLine> headerCells = split(rowLines.get(0));
        int headerColumns = headerCells.size();
        for (int i = 0; i < headerColumns; i++) {
            SourceLine cell = headerCells.get(i);
            TableCell tableCell = parseCell(cell, i, inlineParser);
            tableCell.setHeader(true);
            headerRow.appendChild(tableCell);
        }

        TableBody body = null;
        // Body starts at index 2. 0 is header, 1 is separator.
        for (int rowIndex = 2; rowIndex < rowLines.size(); rowIndex++) {
            SourceLine rowLine = rowLines.get(rowIndex);
            SourceSpan sourceSpan = rowIndex < sourceSpans.size() ? sourceSpans.get(rowIndex) : null;
            List<SourceLine> cells = split(rowLine);
            TableRow row = new TableRow();
            if (sourceSpan != null) {
                row.addSourceSpan(sourceSpan);
            }

            // Body can not have more columns than head
            for (int i = 0; i < headerColumns; i++) {
                SourceLine cell = i < cells.size() ? cells.get(i) : SourceLine.of("", null);
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

    private TableCell parseCell(SourceLine cell, int column, InlineParser inlineParser) {
        TableCell tableCell = new TableCell();
        SourceSpan sourceSpan = cell.getSourceSpan();
        if (sourceSpan != null) {
            tableCell.addSourceSpan(sourceSpan);
        }

        if (column < columns.size()) {
            tableCell.setAlignment(columns.get(column));
        }

        CharSequence content = cell.getContent();
        int start = Parsing.skipSpaceTab(content, 0, content.length());
        int end = Parsing.skipSpaceTabBackwards(content, content.length() - 1, start);
        inlineParser.parse(SourceLines.of(cell.substring(start, end + 1)), tableCell);

        return tableCell;
    }

    private static List<SourceLine> split(SourceLine line) {
        CharSequence row = line.getContent();
        int nonSpace = Parsing.skipSpaceTab(row, 0, row.length());
        int cellStart = nonSpace;
        int cellEnd = row.length();
        if (row.charAt(nonSpace) == '|' && nonSpace + 1 < cellEnd) {
            // This row has leading/trailing pipes - skip the leading pipe
            cellStart = nonSpace + 1;
            // Strip whitespace from the end but not the pipe or we could miss an empty ("||") cell
            int nonSpaceEnd = Parsing.skipSpaceTabBackwards(row, row.length() - 1, cellStart + 1);
            cellEnd = nonSpaceEnd + 1;
        }
        List<SourceLine> cells = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (int i = cellStart; i < cellEnd; i++) {
            char c = row.charAt(i);
            switch (c) {
                case '\\':
                    if (i + 1 < cellEnd && row.charAt(i + 1) == '|') {
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

                    cells.add(SourceLine.of(content, line.substring(cellStart, i).getSourceSpan()));
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
            cells.add(SourceLine.of(content, line.substring(cellStart, line.getContent().length()).getSourceSpan()));
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
            List<SourceLine> paragraphLines = matchedBlockParser.getParagraphLines().getLines();
            if (paragraphLines.size() == 1 && Parsing.find('|', paragraphLines.get(0).getContent(), 0) != -1) {
                SourceLine line = state.getLine();
                SourceLine separatorLine = line.substring(state.getIndex(), line.getContent().length());
                List<TableCell.Alignment> columns = parseSeparator(separatorLine.getContent());
                if (columns != null && !columns.isEmpty()) {
                    SourceLine paragraph = paragraphLines.get(0);
                    List<SourceLine> headerCells = split(paragraph);
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
}
