package org.commonmark.ext.gfm.tables;

import org.commonmark.internal.*;
import org.commonmark.node.Block;
import org.commonmark.node.Node;
import org.commonmark.node.SourcePosition;
import org.commonmark.parser.BlockContinue;
import org.commonmark.parser.BlockStart;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TableBlockParser extends AbstractBlockParser {

    private static Pattern TABLE_HEADER_SEPARATOR = Pattern.compile("^\\|?(?:\\s*:?-{3,}:?\\s*\\|)+\\s*:?-{3,}:?\\s*\\|?\\s*$");

    private final TableBlock block = new TableBlock();
    private final List<CharSequence> rowLines = new ArrayList<>();

    private boolean nextIsSeparatorLine = true;
    private String separatorLine = "";

    private TableBlockParser(CharSequence headerLine, SourcePosition sourcePosition) {
        rowLines.add(headerLine);
        block.setSourcePosition(sourcePosition);
    }

    @Override
    public BlockContinue tryContinue(ParserState state) {
        if (state.getLine().toString().contains("|")) {
            return BlockContinue.of(state.getIndex());
        } else {
            return BlockContinue.none();
        }
    }

    @Override
    public void addLine(CharSequence line) {
        if (nextIsSeparatorLine) {
            nextIsSeparatorLine = false;
            separatorLine = line.toString();
        } else {
            rowLines.add(line);
        }
    }

    @Override
    public void processInlines(InlineParser inlineParser) {
        Node section = new TableHead();
        block.appendChild(section);

        List<TableCell.Alignment> alignments = parseAlignment(separatorLine);

        int headerColumns = -1;
        boolean header = true;
        for (CharSequence rowLine : rowLines) {
            List<String> cells = split(rowLine);
            TableRow tableRow = new TableRow();

            if (headerColumns == -1) {
                headerColumns = cells.size();
            }

            // Body can not have more columns than head
            for (int i = 0; i < headerColumns; i++) {
                String cell = i < cells.size() ? cells.get(i) : "";
                TableCell.Alignment alignment = i < alignments.size() ? alignments.get(i) : null;
                TableCell tableCell = new TableCell();
                tableCell.setHeader(header);
                tableCell.setAlignment(alignment);
                inlineParser.parse(tableCell, cell.trim());
                tableRow.appendChild(tableCell);
            }

            section.appendChild(tableRow);

            if (header) {
                // Format allows only one row in head
                header = false;
                section = new TableBody();
                block.appendChild(section);
            }
        }
    }

    @Override
    public Block getBlock() {
        return block;
    }

    private static List<TableCell.Alignment> parseAlignment(String separatorLine) {
        List<String> parts = split(separatorLine);
        List<TableCell.Alignment> alignments = new ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            boolean left = trimmed.startsWith(":");
            boolean right = trimmed.endsWith(":");
            TableCell.Alignment alignment = getAlignment(left, right);
            alignments.add(alignment);
        }
        return alignments;
    }

    private static List<String> split(CharSequence input) {
        String line = input.toString().trim();
        if (line.startsWith("|")) {
            line = line.substring(1);
        }
        List<String> cells = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean escape = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (escape) {
                escape = false;
                sb.append(c);
            } else {
                switch (c) {
                    case '\\':
                        escape = true;
                        // Removing the escaping '\' is handled by the inline parser later, so add it to cell
                        sb.append(c);
                        break;
                    case '|':
                        cells.add(sb.toString());
                        sb.setLength(0);
                        break;
                    default:
                        sb.append(c);
                }
            }
        }
        if (sb.length() > 0) {
            cells.add(sb.toString());
        }
        return cells;
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
            CharSequence paragraphStartLine = matchedBlockParser.getParagraphStartLine();
            if (paragraphStartLine != null && paragraphStartLine.toString().contains("|")) {
                CharSequence separatorLine = line.subSequence(state.getIndex(), line.length());
                if (TABLE_HEADER_SEPARATOR.matcher(separatorLine).find()) {
                    List<String> headParts = split(paragraphStartLine);
                    List<String> separatorParts = split(separatorLine);
                    if (separatorParts.size() >= headParts.size()) {
                        SourcePosition sourcePosition = state.getActiveBlockParser().getBlock().getSourcePosition();
                        return BlockStart.of(new TableBlockParser(paragraphStartLine, sourcePosition), state.getIndex(), true);
                    }
                }
            }
            return BlockStart.none();
        }
    }

}
