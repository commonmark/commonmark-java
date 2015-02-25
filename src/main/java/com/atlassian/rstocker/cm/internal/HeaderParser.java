package com.atlassian.rstocker.cm.internal;

import com.atlassian.rstocker.cm.node.Block;
import com.atlassian.rstocker.cm.node.Header;

public class HeaderParser extends AbstractBlockParser {

	private final Header block = new Header();

	public HeaderParser(int level) {
		block.setLevel(level);
	}

	@Override
	public ContinueResult parseLine(String line, int nextNonSpace, int[] offset, boolean blank) {
		// a header can never container > 1 line, so fail to match
		return ContinueResult.NOT_MATCHED;
	}

	@Override
	public void addLine(String line) {
	}

	@Override
	public Block getBlock() {
		return block;
	}
}
