package org.commonmark.extras.tables;

import org.commonmark.internal.*;
import org.commonmark.node.Block;
import org.commonmark.node.Node;
import org.commonmark.node.SourcePosition;

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
    public ContinueResult continueBlock(CharSequence line, int nextNonSpace, int offset, boolean blank) {
        if (line.toString().contains("|")) {
            return blockMatched(offset);
        } else {
            return blockDidNotMatch();
        }
    }

    @Override
    public boolean acceptsLine() {
        return true;
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

        boolean header = true;
        for (CharSequence rowLine : rowLines) {
            String[] cells = split(rowLine);
            TableRow tableRow = new TableRow();

            for (int i = 0; i < cells.length; i++) {
                String cell = cells[i];
                TableCell.Alignment alignment = i < alignments.size() ? alignments.get(i) : null;
                TableCell tableCell = new TableCell();
                tableCell.setHeader(header);
                tableCell.setAlignment(alignment);
                inlineParser.parse(tableCell, cell.trim());
                tableRow.appendChild(tableCell);
            }

            section.appendChild(tableRow);

            if (header) {
                // Format allows only one header row
                header = false;
                section = new TableBody();
                block.appendChild(section);
            }
        }
    }

    private static String[] split(CharSequence input) {
        String line = input.toString();
        if (line.startsWith("|")) {
            line = line.substring(1);
        }
        return line.split("\\|");
    }

    private static List<TableCell.Alignment> parseAlignment(String separatorLine) {
        String[] parts = split(separatorLine);
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

    @Override
    public Block getBlock() {
        return block;
    }

    public static class Factory extends AbstractBlockParserFactory {

        @Override
        public StartResult tryStart(ParserState state) {
            CharSequence line = state.getLine();
            CharSequence paragraphStartLine = state.getParagraphStartLine();
            if (paragraphStartLine != null && paragraphStartLine.toString().contains("|")) {
                CharSequence separatorLine = line.subSequence(state.getOffset(), line.length());
                if (TABLE_HEADER_SEPARATOR.matcher(separatorLine).find()) {
                    SourcePosition sourcePosition = state.getActiveBlockParser().getBlock().getSourcePosition();
                    return start(new TableBlockParser(paragraphStartLine, sourcePosition), state.getOffset(), true);
                }
            }
            return noStart();
        }
    }

}
