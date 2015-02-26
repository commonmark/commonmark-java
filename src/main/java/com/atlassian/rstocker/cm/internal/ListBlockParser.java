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
	public ContinueResult continueBlock(String line, int nextNonSpace, int[] offset, boolean blank) {
		return ContinueResult.MATCHED;
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
}
