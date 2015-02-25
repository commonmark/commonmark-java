package com.atlassian.rstocker.cm.internal;

import com.atlassian.rstocker.cm.node.Block;
import com.atlassian.rstocker.cm.node.ListItem;
import com.atlassian.rstocker.cm.node.Node;

public class ListItemParser extends AbstractBlockParser {

	private final ListItem block = new ListItem();

	private int itemOffset;

	public ListItemParser(int itemOffset) {
		this.itemOffset = itemOffset;
	}

	@Override
	public ContinueResult parseLine(String line, int nextNonSpace, int[] offset, boolean blank) {
		int indent = nextNonSpace - offset[0];
		if (blank) {
			offset[0] = nextNonSpace;
			return ContinueResult.MATCHED;
		} else if (indent >= itemOffset) {
			offset[0] += itemOffset;
			return ContinueResult.MATCHED;
		} else {
			return ContinueResult.NOT_MATCHED;
		}
	}

	@Override
	public boolean canContain(Node.Type type) {
		return true;
	}

	@Override
	public Block getBlock() {
		return block;
	}
}
