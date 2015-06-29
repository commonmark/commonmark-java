package org.commonmark.internal;

import org.commonmark.node.*;
import org.commonmark.parser.BlockContinue;
import org.commonmark.parser.BlockStart;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ListBlockParser extends AbstractBlockParser {

    private static Pattern BULLET_LIST_MARKER = Pattern.compile("^[*+-]( +|$)");
    private static Pattern ORDERED_LIST_MARKER = Pattern.compile("^(\\d+)([.)])( +|$)");

    private final ListBlock block;

    public ListBlockParser(ListBlock block, SourcePosition pos) {
        this.block = block;
        block.setSourcePosition(pos);
    }

    @Override
    public BlockContinue tryContinue(ParserState state) {
        // List blocks themselves don't have any markers, only list items. So try to stay in the list.
        // If there is a block start other than list item, canContain makes sure that this list is closed.
        return BlockContinue.of(state.getIndex());
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

    public void setTight(boolean tight) {
        block.setTight(tight);
    }

    /**
     * Parse a list marker and return data on the marker or null.
     */
    private static ListData parseListMarker(CharSequence ln, int offset, int indent) {
        CharSequence rest = ln.subSequence(offset, ln.length());
        int spacesAfterMarker;
        ListBlock listBlock;

        Matcher match;
        if ((match = BULLET_LIST_MARKER.matcher(rest)).find()) {
            BulletList bulletList = new BulletList();
            bulletList.setBulletMarker(match.group(0).charAt(0));
            listBlock = bulletList;
            spacesAfterMarker = match.group(1).length();
        } else if ((match = ORDERED_LIST_MARKER.matcher(rest)).find()) {
            OrderedList orderedList = new OrderedList();
            orderedList.setStartNumber(Integer.parseInt(match.group(1)));
            orderedList.setDelimiter(match.group(2).charAt(0));
            listBlock = orderedList;
            spacesAfterMarker = match.group(3).length();
        } else {
            return null;
        }
        int padding;
        boolean blankItem = match.group(0).length() == rest.length();
        if (spacesAfterMarker >= 5 || spacesAfterMarker < 1 || blankItem) {
            padding = match.group(0).length() - spacesAfterMarker + 1;
        } else {
            padding = match.group(0).length();
        }
        return new ListData(listBlock, indent, padding);
    }

    /**
     * Returns true if the two list items are of the same type,
     * with the same delimiter and bullet character. This is used
     * in agglomerating list items into lists.
     */
    private static boolean listsMatch(ListBlock a, ListBlock b) {
        if (a instanceof BulletList && b instanceof BulletList) {
            return Objects.equals(((BulletList) a).getBulletMarker(), ((BulletList) b).getBulletMarker());
        } else if (a instanceof OrderedList && b instanceof OrderedList) {
            return Objects.equals(((OrderedList) a).getDelimiter(), ((OrderedList) b).getDelimiter());
        }
        return false;
    }

    private static class ListData {
        final ListBlock listBlock;
        final int indent;
        final int padding;

        public ListData(ListBlock listBlock, int indent, int padding) {
            this.listBlock = listBlock;
            this.indent = indent;
            this.padding = padding;
        }
    }

    public static class Factory extends AbstractBlockParserFactory {

        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            int nextNonSpace = state.getNextNonSpaceIndex();
            int indent = nextNonSpace - state.getIndex();
            ListData listData = parseListMarker(state.getLine(), nextNonSpace, indent);
            if (listData == null) {
                return BlockStart.none();
            }

            // list item
            int newOffset = nextNonSpace + listData.padding;

            List<BlockParser> blockParsers = new ArrayList<>(2);

            // add the list if needed
            BlockParser matched = matchedBlockParser.getMatchedBlockParser();
            if (!(matched instanceof ListBlockParser) ||
                    !(listsMatch((ListBlock) matched.getBlock(), listData.listBlock))) {

                ListBlockParser listBlockParser = new ListBlockParser(listData.listBlock, pos(state, nextNonSpace));
                listBlockParser.setTight(true);

                blockParsers.add(listBlockParser);
            }

            // add the list item
            int itemOffset = listData.indent + listData.padding;
            blockParsers.add(new ListItemParser(itemOffset, pos(state, nextNonSpace)));

            return BlockStart.of(blockParsers, newOffset, false);
        }
    }
}
