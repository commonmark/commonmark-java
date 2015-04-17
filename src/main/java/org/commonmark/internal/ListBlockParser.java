package org.commonmark.internal;

import org.commonmark.node.*;

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
    public ContinueResult continueBlock(String line, int nextNonSpace, int offset, boolean blank) {
        return blockMatched(offset);
    }

    @Override
    public boolean canContain(Block block) {
        return block instanceof ListItem;
    }

    @Override
    public boolean shouldTryBlockStarts() {
        return true;
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
    private static ListData parseListMarker(String ln, int offset, int indent) {
        String rest = ln.substring(offset);
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
        public StartResult tryStart(ParserState state) {
            int nextNonSpace = state.getNextNonSpace();
            int indent = nextNonSpace - state.getOffset();
            ListData listData = parseListMarker(state.getLine(), nextNonSpace, indent);
            if (listData == null) {
                return noStart();
            }

            // list item
            int newOffset = nextNonSpace + listData.padding;

            List<BlockParser> blockParsers = new ArrayList<>(2);

            // add the list if needed
            BlockParser active = state.getActiveBlockParser();
            if (!(active instanceof ListBlockParser) ||
                    !(listsMatch((ListBlock) active.getBlock(), listData.listBlock))) {

                ListBlockParser listBlockParser = new ListBlockParser(listData.listBlock, pos(state, nextNonSpace));
                listBlockParser.setTight(true);

                blockParsers.add(listBlockParser);
            }

            // add the list item
            int itemOffset = listData.indent + listData.padding;
            blockParsers.add(new ListItemParser(itemOffset, pos(state, nextNonSpace)));

            return start(blockParsers, newOffset, false);
        }
    }
}
