package org.commonmark.internal;

import org.commonmark.node.*;
import org.commonmark.parser.block.*;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ListBlockParser extends AbstractBlockParser {

    private static Pattern BULLET_LIST_MARKER = Pattern.compile("^[*+-]( +|$)");
    private static Pattern ORDERED_LIST_MARKER = Pattern.compile("^(\\d{1,9})([.)])( +|$)");

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
    private static ListData parseListMarker(CharSequence ln, int offset) {
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
        return new ListData(listBlock, padding);
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

    public static class Factory extends AbstractBlockParserFactory {

        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            BlockParser matched = matchedBlockParser.getMatchedBlockParser();

            if (state.getIndent() >= 4 && !(matched instanceof ListBlockParser)) {
                return BlockStart.none();
            }
            int nextNonSpace = state.getNextNonSpaceIndex();
            ListData listData = parseListMarker(state.getLine(), nextNonSpace);
            if (listData == null) {
                return BlockStart.none();
            }

            // list item
            int newIndex = nextNonSpace + listData.padding;

            int itemIndent = state.getIndent() + listData.padding;
            ListItemParser listItemParser = new ListItemParser(itemIndent);

            // prepend the list block if needed
            if (!(matched instanceof ListBlockParser) ||
                    !(listsMatch((ListBlock) matched.getBlock(), listData.listBlock))) {

                ListBlockParser listBlockParser = new ListBlockParser(listData.listBlock);
                listBlockParser.setTight(true);

                return BlockStart.of(listBlockParser, listItemParser).atIndex(newIndex);
            } else {
                return BlockStart.of(listItemParser).atIndex(newIndex);
            }
        }
    }

    private static class ListData {
        final ListBlock listBlock;
        final int padding;

        public ListData(ListBlock listBlock, int padding) {
            this.listBlock = listBlock;
            this.padding = padding;
        }
    }

}
