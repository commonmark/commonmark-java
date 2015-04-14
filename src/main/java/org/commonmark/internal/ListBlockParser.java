package org.commonmark.internal;

import org.commonmark.node.Block;
import org.commonmark.node.ListBlock;
import org.commonmark.node.Node;
import org.commonmark.node.SourcePosition;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ListBlockParser extends AbstractBlockParser {

    private static Pattern BULLET_LIST_MARKER = Pattern.compile("^[*+-]( +|$)");
    private static Pattern ORDERED_LIST_MARKER = Pattern.compile("^(\\d+)([.)])( +|$)");

    private final ListBlock block = new ListBlock();

    public ListBlockParser(ListData data, SourcePosition pos) {
        block.setListType(data.type);
        block.setOrderedDelimiter(data.delimiter);
        block.setOrderedStart(data.start);
        block.setBulletMarker(data.bulletChar);
        block.setSourcePosition(pos);
    }

    @Override
    public ContinueResult continueBlock(String line, int nextNonSpace, int offset, boolean blank) {
        return blockMatched(offset);
    }

    @Override
    public boolean canContain(Node.Type type) {
        return type == Node.Type.Item;
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

    // Parse a list marker and return data on the marker (type,
    // start, delimiter, bullet character, padding) or null.
    private static ListData parseListMarker(String ln, int offset, int indent) {
        String rest = ln.substring(offset);
        Matcher match;
        int spaces_after_marker;
        ListData data = new ListData(indent);

        if ((match = BULLET_LIST_MARKER.matcher(rest)).find()) {
            spaces_after_marker = match.group(1).length();
            data.type = ListBlock.ListType.BULLET;
            data.bulletChar = match.group(0).charAt(0);

        } else if ((match = ORDERED_LIST_MARKER.matcher(rest)).find()) {
            spaces_after_marker = match.group(3).length();
            data.type = ListBlock.ListType.ORDERED;
            data.start = Integer.parseInt(match.group(1));
            data.delimiter = match.group(2).charAt(0);
        } else {
            return null;
        }
        boolean blank_item = match.group(0).length() == rest.length();
        if (spaces_after_marker >= 5 ||
                spaces_after_marker < 1 ||
                blank_item) {
            data.padding = match.group(0).length() - spaces_after_marker + 1;
        } else {
            data.padding = match.group(0).length();
        }
        return data;
    }

    // Returns true if the two list items are of the same type,
    // with the same delimiter and bullet character. This is used
    // in agglomerating list items into lists.
    private static boolean listsMatch(ListBlock list, ListData item_data) {
        return (Objects.equals(list.getListType(), item_data.type) &&
                list.getOrderedDelimiter() == item_data.delimiter &&
                list.getBulletMarker() == item_data.bulletChar);
    }

    public static class Factory extends AbstractBlockParserFactory {

        @Override
        public StartResult tryStart(ParserState state) {
            int nextNonSpace = state.getNextNonSpace();
            int indent = nextNonSpace - state.getOffset();
            ListData listData;
            if ((listData = parseListMarker(state.getLine(), nextNonSpace, indent)) != null) {
                // list item
                int newOffset = nextNonSpace + listData.padding;

                List<BlockParser> blockParsers = new ArrayList<>(2);

                // add the list if needed
                BlockParser active = state.getActiveBlockParser();
                if (!(active instanceof ListBlockParser) ||
                        !(listsMatch((ListBlock) active.getBlock(), listData))) {
                    ListBlockParser listBlockParser = new ListBlockParser(listData, pos(state, nextNonSpace));
                    listBlockParser.setTight(true);

                    blockParsers.add(listBlockParser);
                }

                // add the list item
                int itemOffset = listData.markerOffset + listData.padding;
                blockParsers.add(new ListItemParser(itemOffset, pos(state, nextNonSpace)));

                return start(blockParsers, newOffset, false);
            } else {
                return noStart();
            }
        }
    }
}
