package com.atlassian.rstocker.cm.internal;

import com.atlassian.rstocker.cm.node.Block;
import com.atlassian.rstocker.cm.node.ListBlock;
import com.atlassian.rstocker.cm.node.Node;

public class ListBlockParser extends AbstractBlockParser {

	private final ListBlock block = new ListBlock();

	public ListBlockParser(ListData data) {
		block.setListType(data.type);
		block.setOrderedDelimiter(data.delimiter);
		block.setOrderedStart(data.start);
		block.setBulletMarker(data.bulletChar);
	}

	@Override
	public ContinueResult parseLine(String line, int nextNonSpace, int[] offset, boolean blank) {
		// TODO
		return null;
	}

	@Override
	public void addLine(String line) {

	}

	@Override
	public void finalizeBlock(InlineParser inlineParser) {
		Node item = block.getFirstChild();
		while (item != null) {
			// check for non-final list item ending with blank line:
			if (endsWithBlankLine(item) && item.getNext() != null) {
				block.setTight(false);
				break;
			}
			// recurse into children of list item, to see if there are
			// spaces between any of them:
			Node subitem = item.getFirstChild();
			while (subitem != null) {
				if (endsWithBlankLine(subitem) && (item.getNext() != null || subitem.getNext() != null)) {
					block.setTight(false);
					break;
				}
				subitem = subitem.getNext();
			}
			item = item.getNext();
		}
	}

	@Override
	public boolean canContain(Node.Type type) {
		return type == Node.Type.Item;
	}

	@Override
	public Block getBlock() {
		return block;
	}

	public void setTight(boolean tight) {
		block.setTight(tight);
	}

	// Returns true if block ends with a blank line, descending if needed
	// into lists and sublists.
	private boolean endsWithBlankLine(Node block) {
		// FIXME
		return false;
//		while (block != null) {
//			if (isLastLineBlank(block)) {
//				return true;
//			}
//			Node.Type t = block.getType();
//			if (t == Node.Type.List || t == Node.Type.Item) {
//				block = block.getLastChild();
//			} else {
//				break;
//			}
//		}
//		return false;
	}
}
