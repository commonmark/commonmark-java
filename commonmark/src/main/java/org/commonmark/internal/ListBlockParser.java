package org.commonmark.internal;

import org.commonmark.internal.util.Parsing;
import org.commonmark.node.*;
import org.commonmark.parser.block.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ListBlockParser extends AbstractBlockParser {

    private static Pattern MARKER = Pattern.compile(
            "^([*+-])(?= |\t|$)" +
                    "|^(\\d{1,9})([.)])(?= |\t|$)");

    private final ListBlock block;

    public ListBlockParser(ListBlock block) {
        this.block = block;
    }

    @Override
    public boolean isContainer() {
        return true;
    }

    @Override
    public boolean canContain(Block block) {
        return block instanceof ListItem;
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public BlockContinue tryContinue(ParserState state) {
        // List blocks themselves don't have any markers, only list items. So try to stay in the list.
        // If there is a block start other than list item, canContain makes sure that this list is closed.
        return BlockContinue.atIndex(state.getIndex());
    }

    public void setTight(boolean tight) {
        block.setTight(tight);
    }

    /**
     * Parse a list marker and return data on the marker or null.
     */
    private static ListData parseListMarker(CharSequence line, final int markerIndex, final int markerColumn,
                                            final boolean inParagraph) {
        CharSequence rest = line.subSequence(markerIndex, line.length());
        Matcher matcher = MARKER.matcher(rest);
        if (!matcher.find()) {
            return null;
        }

        ListBlock listBlock = createListBlock(matcher);

        int markerLength = matcher.end() - matcher.start();
        int indexAfterMarker = markerIndex + markerLength;
        // marker doesn't include tabs, so counting them as columns directly is ok
        int columnAfterMarker = markerColumn + markerLength;
        // the column within the line where the content starts
        int contentColumn = columnAfterMarker;

        // See at which column the content starts if there is content
        boolean hasContent = false;
        for (int i = indexAfterMarker; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\t') {
                contentColumn += Parsing.columnsToNextTabStop(contentColumn);
            } else if (c == ' ') {
                contentColumn++;
            } else {
                hasContent = true;
                break;
            }
        }

        if (inParagraph) {
            // If the list item is ordered, the start number must be 1 to interrupt a paragraph.
            if (listBlock instanceof OrderedList && ((OrderedList) listBlock).getStartNumber() != 1) {
                return null;
            }
            // Empty list item can not interrupt a paragraph.
            if (!hasContent) {
                return null;
            }
        }

        if (!hasContent || (contentColumn - columnAfterMarker) > Parsing.CODE_BLOCK_INDENT) {
            // If this line is blank or has a code block, default to 1 space after marker
            contentColumn = columnAfterMarker + 1;
        }

        return new ListData(listBlock, contentColumn);
    }

    private static ListBlock createListBlock(Matcher matcher) {
        String bullet = matcher.group(1);
        if (bullet != null) {
            BulletList bulletList = new BulletList();
            bulletList.setBulletMarker(bullet.charAt(0));
            return bulletList;
        } else {
            String digit = matcher.group(2);
            String delim = matcher.group(3);
            OrderedList orderedList = new OrderedList();
            orderedList.setStartNumber(Integer.parseInt(digit));
            orderedList.setDelimiter(delim.charAt(0));
            return orderedList;
        }
    }

    /**
     * Returns true if the two list items are of the same type,
     * with the same delimiter and bullet character. This is used
     * in agglomerating list items into lists.
     */
    private static boolean listsMatch(ListBlock a, ListBlock b) {
        if (a instanceof BulletList && b instanceof BulletList) {
            return equals(((BulletList) a).getBulletMarker(), ((BulletList) b).getBulletMarker());
        } else if (a instanceof OrderedList && b instanceof OrderedList) {
            return equals(((OrderedList) a).getDelimiter(), ((OrderedList) b).getDelimiter());
        }
        return false;
    }

    private static boolean equals(Object a, Object b) {
        return (a == null) ? (b == null) : a.equals(b);
    }

    public static class Factory extends AbstractBlockParserFactory {

        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            BlockParser matched = matchedBlockParser.getMatchedBlockParser();

            if (state.getIndent() >= Parsing.CODE_BLOCK_INDENT && !(matched instanceof ListBlockParser)) {
                return BlockStart.none();
            }
            int markerIndex = state.getNextNonSpaceIndex();
            int markerColumn = state.getColumn() + state.getIndent();
            boolean inParagraph = matchedBlockParser.getParagraphContent() != null;
            ListData listData = parseListMarker(state.getLine(), markerIndex, markerColumn, inParagraph);
            if (listData == null) {
                return BlockStart.none();
            }

            int newColumn = listData.contentColumn;
            ListItemParser listItemParser = new ListItemParser(newColumn - state.getColumn());

            // prepend the list block if needed
            if (!(matched instanceof ListBlockParser) ||
                    !(listsMatch((ListBlock) matched.getBlock(), listData.listBlock))) {

                ListBlockParser listBlockParser = new ListBlockParser(listData.listBlock);
                listBlockParser.setTight(true);

                return BlockStart.of(listBlockParser, listItemParser).atColumn(newColumn);
            } else {
                return BlockStart.of(listItemParser).atColumn(newColumn);
            }
        }
    }

    private static class ListData {
        final ListBlock listBlock;
        final int contentColumn;

        ListData(ListBlock listBlock, int contentColumn) {
            this.listBlock = listBlock;
            this.contentColumn = contentColumn;
        }
    }
}
