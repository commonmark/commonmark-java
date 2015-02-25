package com.atlassian.rstocker.cm.internal;

import com.atlassian.rstocker.cm.node.Block;
import com.atlassian.rstocker.cm.node.HorizontalRule;

public class HorizontalRuleParser extends AbstractBlockParser {

	private final HorizontalRule block = new HorizontalRule();

	@Override
	public ContinueResult parseLine(String line, int nextNonSpace, int[] offset, boolean blank) {
		// a horizontal rule can never container > 1 line, so fail to match
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
