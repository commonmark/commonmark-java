package org.commonmark.extras.tables;

import org.commonmark.internal.*;
import org.commonmark.node.Block;
import org.commonmark.node.Node;
import org.commonmark.node.SourcePosition;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TableBlockParser extends AbstractBlockParser {

    private static Pattern TABLE_HEADER_SEPARATOR = Pattern.compile("^\\|?(?:\\s*-{3,}\\s*\\|)+\\s*-{3,}\\s*\\|?\\s*$");

    private final TableBlock block = new TableBlock();
    private final List<CharSequence> rowLines = new ArrayList<>();

    private boolean separatorLine = true;

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
        if (separatorLine) {
            separatorLine = false;
        } else {
            rowLines.add(line);
        }
    }

    @Override
    public void processInlines(InlineParser inlineParser) {
        Node section = new TableHead();
        block.appendChild(section);

        boolean header = true;
        for (CharSequence rowLine : rowLines) {
            String s = rowLine.toString();
            if (s.startsWith("|")) {
                s = s.substring(1);
            }
            String[] cells = s.split("\\|");
            TableRow tableRow = new TableRow();

            for (String cell : cells) {
                TableCell tableCell = new TableCell();
                tableCell.setHeader(header);
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
                CharSequence lineAfterOffset = line.subSequence(state.getOffset(), line.length());
                if (TABLE_HEADER_SEPARATOR.matcher(lineAfterOffset).find()) {
                    SourcePosition sourcePosition = state.getActiveBlockParser().getBlock().getSourcePosition();
                    return start(new TableBlockParser(paragraphStartLine, sourcePosition), line.length(), true);
                }
            }
            return noStart();
        }
    }

}
